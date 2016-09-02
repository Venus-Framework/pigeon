/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ServiceConfigUtils;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.pigeon.util.ClassUtils;
import com.dianping.pigeon.util.LangUtils;

public class AnnotationBean implements DisposableBean, BeanFactoryPostProcessor, BeanPostProcessor,
		ApplicationContextAware {

	private static final Logger logger = LoggerLoader.getLogger(AnnotationBean.class);

	private String annotationPackage = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.provider.interface.packages", "com.dianping");

	private String[] annotationPackages = new String[] { "com.dianping" };

	private final ConcurrentMap<String, InvokerConfig<?>> invokerConfigs = new ConcurrentHashMap<String, InvokerConfig<?>>();

	public String getPackage() {
		return annotationPackage;
	}

	public void setPackage(String annotationPackage) {
		this.annotationPackage = annotationPackage;
		this.annotationPackages = (annotationPackage == null || annotationPackage.length() == 0) ? null
				: Constants.COMMA_SPLIT_PATTERN.split(annotationPackage);
	}

	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (annotationPackage == null || annotationPackage.length() == 0) {
			return;
		}
		if (beanFactory instanceof BeanDefinitionRegistry) {
			try {
				// init scanner
				Class<?> scannerClass = ClassUtils
						.loadClass("org.springframework.context.annotation.ClassPathBeanDefinitionScanner");
				Object scanner = scannerClass.getConstructor(
						new Class<?>[] { BeanDefinitionRegistry.class, boolean.class }).newInstance(
						new Object[] { (BeanDefinitionRegistry) beanFactory, true });
				// add filter
				Class<?> filterClass = ClassUtils
						.loadClass("org.springframework.core.type.filter.AnnotationTypeFilter");
				Object filter = filterClass.getConstructor(Class.class).newInstance(Service.class);
				Method addIncludeFilter = scannerClass.getMethod("addIncludeFilter",
						ClassUtils.loadClass("org.springframework.core.type.filter.TypeFilter"));
				addIncludeFilter.invoke(scanner, filter);
				// scan packages
				String[] packages = Constants.COMMA_SPLIT_PATTERN.split(annotationPackage);
				Method scan = scannerClass.getMethod("scan", new Class<?>[] { String[].class });
				scan.invoke(scanner, new Object[] { packages });
			} catch (Throwable e) {
				// spring 2.0
			}
		}
	}

	public int getDefaultPort(int port) {
		if (port == 4040) {
			try {
				String app = ConfigManagerLoader.getConfigManager().getAppName();
				if (StringUtils.isNotBlank(app)) {
					return LangUtils.hash(app, 6000, 2000);
				}
			} catch (Throwable t) {
			}
		}
		return port;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (!isMatchPackage(bean)) {
			return bean;
		}
		Class<?> beanClass = bean.getClass();
		int idxCglib = beanClass.getName().contains("$$EnhancerByCGLIB") ?
				beanClass.getName().indexOf("$$EnhancerByCGLIB") : beanClass.getName().indexOf("$$EnhancerBySpringCGLIB");
		if (idxCglib != -1) {
			try {
				beanClass = ClassUtils.loadClass(beanClass.getName().substring(0, idxCglib));
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Failed to export remote service class " + beanClass.getName(), e);
			}
		}
		Service service = beanClass.getAnnotation(Service.class);
		if (service != null) {
			Class serviceInterface = service.interfaceClass();
			if (void.class.equals(service.interfaceClass())) {
				serviceInterface = ServiceConfigUtils.getServiceInterface(beanClass);
			}
			if (serviceInterface == null) {
				serviceInterface = beanClass;
			}
			ProviderConfig<Object> providerConfig = new ProviderConfig<Object>(serviceInterface, bean);
			providerConfig.setService(bean);
			providerConfig.setUrl(service.url());
			providerConfig.setVersion(service.version());
			providerConfig.setSharedPool(service.useSharedPool());
			providerConfig.setActives(service.actives());

			ServerConfig serverConfig = new ServerConfig();
			serverConfig.setPort(getDefaultPort(service.port()));
			serverConfig.setGroup(service.group());
			serverConfig.setAutoSelectPort(service.autoSelectPort());
			providerConfig.setServerConfig(serverConfig);
			ServiceFactory.addService(providerConfig);
		}
		postProcessBeforeInitialization(bean, beanName);
		return bean;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (!isMatchPackage(bean)) {
			return bean;
		}
		Method[] methods = bean.getClass().getMethods();
		for (Method method : methods) {
			String name = method.getName();
			if (name.length() > 3 && name.startsWith("set") && method.getParameterTypes().length == 1
					&& Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
				try {
					Reference reference = method.getAnnotation(Reference.class);
					if (reference != null) {
						Object value = refer(reference, method.getParameterTypes()[0]);
						if (value != null) {
							method.invoke(bean, new Object[] {});
						}
					}
				} catch (Throwable e) {
					logger.error("Failed to init remote service reference at method " + name + " in class "
							+ bean.getClass().getName() + ", cause: " + e.getMessage(), e);
				}
			}
		}
		Class<?> superClass = bean.getClass().getSuperclass();
		while (superClass != null && isMatchPackage(superClass)) {
			referFields(bean, superClass.getDeclaredFields());
			superClass = superClass.getSuperclass();
		}
		referFields(bean, bean.getClass().getDeclaredFields());

		return bean;
	}

	private void referFields(Object bean, Field[] fields) {
		for (Field field : fields) {
			try {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				Reference reference = field.getAnnotation(Reference.class);
				if (reference != null) {
					Object value = refer(reference, field.getType());
					if (value != null) {
						field.set(bean, value);
					}
				}
			} catch (Throwable e) {
				logger.error("Failed to init remote service reference at field " + field.getName() + " in class "
						+ bean.getClass().getName() + ", cause: " + e.getMessage(), e);
			}
		}
	}

	private Object refer(Reference reference, Class<?> referenceClass) { // method.getParameterTypes()[0]
		String interfaceName;
		if (!void.class.equals(reference.interfaceClass())) {
			interfaceName = reference.interfaceClass().getName();
		} else if (referenceClass.isInterface()) {
			interfaceName = referenceClass.getName();
		} else {
			throw new IllegalStateException(
					"The @Reference undefined interfaceClass or interfaceName, and the property type "
							+ referenceClass.getName() + " is not a interface.");
		}
		String callbackClassName = reference.callback();
		ServiceCallback callback = null;
		if (StringUtils.isNotBlank(callbackClassName)) {
			Class<?> clazz;
			try {
				clazz = ClassUtils.loadClass(callbackClassName);
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("The @Reference undefined callback " + callbackClassName
						+ ", is not a ServiceCallback interface.");
			}
			if (!ServiceCallback.class.isAssignableFrom(clazz)) {
				throw new IllegalStateException("The @Reference undefined callback " + callbackClassName
						+ ", is not a ServiceCallback interface.");
			}
			try {
				callback = (ServiceCallback) clazz.newInstance();
			} catch (InstantiationException e) {
				throw new IllegalStateException("The @Reference undefined callback " + callbackClassName
						+ ", is not a ServiceCallback interface.");
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("The @Reference undefined callback " + callbackClassName
						+ ", is not a ServiceCallback interface.");
			}
		}
		String key = reference.group() + "/" + reference.url() + "@" + interfaceName + ":" + reference.version() + ":"
				+ reference.serialize() + ":" + reference.protocol() + ":" + reference.timeout() + ":"
				+ reference.callType();
		InvokerConfig<?> invokerConfig = invokerConfigs.get(key);
		if (invokerConfig == null) {
			invokerConfig = new InvokerConfig(referenceClass, reference.url(), reference.timeout(),
					reference.callType(), reference.serialize(), callback, reference.group(), false,
					reference.loadbalance(), reference.cluster(), reference.retries(), reference.timeoutRetry(),
					reference.vip(), reference.version(), reference.protocol());
			invokerConfig.setSecret(reference.secret());
			invokerConfigs.putIfAbsent(key, invokerConfig);
			invokerConfig = invokerConfigs.get(key);
		}
		return ServiceFactory.getService(invokerConfig);
	}

	private boolean isMatchPackage(Object bean) {
		if (annotationPackages == null || annotationPackages.length == 0) {
			return true;
		}
		String beanClassName = bean.getClass().getName();
		for (String pkg : annotationPackages) {
			if (beanClassName.startsWith(pkg)) {
				return true;
			}
		}
		return false;
	}

	private boolean isMatchPackage(Class type) {
		String beanClassName = type.getName();
		for (String pkg : annotationPackages) {
			if (beanClassName.startsWith(pkg)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void destroy() throws Exception {

	}

}
