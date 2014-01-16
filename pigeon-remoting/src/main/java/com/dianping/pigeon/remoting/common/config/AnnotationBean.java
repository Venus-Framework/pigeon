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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

public class AnnotationBean implements DisposableBean, BeanFactoryPostProcessor, BeanPostProcessor,
		ApplicationContextAware {

	private static final Logger logger = LoggerLoader.getLogger(AnnotationBean.class);

	public static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

	private String annotationPackage = "com.dianping";

	private String[] annotationPackages = new String[] { "com.dianping" };

	private final ConcurrentMap<String, InvokerConfig<?>> invokerConfigs = new ConcurrentHashMap<String, InvokerConfig<?>>();

	private final ConcurrentMap<String, ProviderConfig<?>> providerConfigs = new ConcurrentHashMap<String, ProviderConfig<?>>();

	public String getPackage() {
		return annotationPackage;
	}

	public void setPackage(String annotationPackage) {
		this.annotationPackage = annotationPackage;
		this.annotationPackages = (annotationPackage == null || annotationPackage.length() == 0) ? null
				: COMMA_SPLIT_PATTERN.split(annotationPackage);
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
				Class<?> scannerClass = Class
						.forName("org.springframework.context.annotation.ClassPathBeanDefinitionScanner");
				Object scanner = scannerClass.getConstructor(
						new Class<?>[] { BeanDefinitionRegistry.class, boolean.class }).newInstance(
						new Object[] { (BeanDefinitionRegistry) beanFactory, true });
				// add filter
				Class<?> filterClass = Class.forName("org.springframework.core.type.filter.AnnotationTypeFilter");
				Object filter = filterClass.getConstructor(Class.class).newInstance(Service.class);
				Method addIncludeFilter = scannerClass.getMethod("addIncludeFilter",
						Class.forName("org.springframework.core.type.filter.TypeFilter"));
				addIncludeFilter.invoke(scanner, filter);
				// scan packages
				String[] packages = COMMA_SPLIT_PATTERN.split(annotationPackage);
				Method scan = scannerClass.getMethod("scan", new Class<?>[] { String[].class });
				scan.invoke(scanner, new Object[] { packages });
			} catch (Throwable e) {
				// spring 2.0
			}
		}
	}

	public void destroy() throws Exception {
		for (InvokerConfig<?> referenceConfig : invokerConfigs.values()) {
			try {
				// referenceConfig.destroy();
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}
		for (ProviderConfig<?> providerConfig : providerConfigs.values()) {
			try {

			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (!isMatchPackage(bean)) {
			return bean;
		}
		Service service = bean.getClass().getAnnotation(Service.class);
		if (service != null) {
			Class serviceInterface = service.interfaceClass();
			if (void.class.equals(service.interfaceClass())) {
				if (bean.getClass().getInterfaces().length > 0) {
					serviceInterface = bean.getClass().getInterfaces()[0];
				} else {
					throw new IllegalStateException(
							"Failed to export remote service class "
									+ bean.getClass().getName()
									+ ", cause: The @Service undefined interfaceClass or interfaceName, and the service class unimplemented any interfaces.");
				}
			}
			ProviderConfig<Object> providerConfig = new ProviderConfig<Object>(serviceInterface, bean);
			providerConfig.setService(bean);
			providerConfig.setUrl(service.url());
			providerConfig.setVersion(service.version());
			ServerConfig serverConfig = new ServerConfig();
			serverConfig.setAutoSelectPort(service.autoRegister());
			serverConfig.setPort(service.port());
			serverConfig.setGroup(service.group());
			serverConfig.setAutoSelectPort(service.autoSelectPort());
			serverConfig.setHttpPort(service.httpPort());
			providerConfig.setServerConfig(serverConfig);

			String key = serverConfig.getGroup() + "/" + providerConfig.getServiceInterface().getName() + ":"
					+ providerConfig.getVersion();
			providerConfigs.put(key, providerConfig);
			try {
				ServiceFactory.publishService(providerConfig);
			} catch (RpcException e) {
				throw new IllegalStateException("Failed to publish remote service class " + bean.getClass().getName()
						+ ", config:" + providerConfig, e);
			}
		}
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
		Field[] fields = bean.getClass().getDeclaredFields();
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
				logger.error("Failed to init remote service reference at filed " + field.getName() + " in class "
						+ bean.getClass().getName() + ", cause: " + e.getMessage(), e);
			}
		}
		return bean;
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
				clazz = Class.forName(callbackClassName);
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
		String key = reference.group() + "/" + interfaceName + ":" + reference.version();
		InvokerConfig<?> invokerConfig = invokerConfigs.get(key);
		if (invokerConfig == null) {
			invokerConfig = new InvokerConfig(referenceClass, reference.url(), reference.timeout(),
					reference.callType(), reference.serialize(), callback, reference.group(), false,
					reference.loadbalance(), reference.cluster(), reference.retries(), reference.timeoutRetry(),
					reference.vip(), reference.version(), reference.protocol());
			invokerConfigs.putIfAbsent(key, invokerConfig);
			invokerConfig = invokerConfigs.get(key);
		}
		try {
			return ServiceFactory.getService(invokerConfig);
		} catch (RpcException e) {
			throw new IllegalStateException("Error while referencing to:" + invokerConfig, e);
		}
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

}
