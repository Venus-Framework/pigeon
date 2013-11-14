/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.config.spring;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.invoker.loader.InvokerBootStrapLoader;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: PigeonBeanDefinitionParser.java, v 0.1 2013-6-24 下午9:58:37
 *          jianhuihuang Exp $
 */
public class InvokerBeanDefinitionParser implements BeanDefinitionParser {

	/** Default placeholder prefix: "${" */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
	/** Default placeholder suffix: "}" */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private static final Logger logger = LoggerLoader.getLogger(InvokerBeanDefinitionParser.class);

	private final Class<?> beanClass;

	private final boolean required;

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public InvokerBeanDefinitionParser(Class<?> beanClass, boolean required) {
		this.beanClass = beanClass;
		this.required = required;
	}

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		return parse(element, parserContext, beanClass, required);
	}

	private static BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass,
			boolean required) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		// beanDefinition.setBeanClass(beanClass);
		beanDefinition.setLazyInit(false);
		String id = element.getAttribute("id");

		if (beanClass == ReferenceBean.class) {
			beanDefinition.setBeanClass(ProxyBeanFactory.class);
			beanDefinition.setInitMethodName("init");
			InvokerBootStrapLoader.startup();

			MutablePropertyValues properties = beanDefinition.getPropertyValues();
			properties.addPropertyValue("serviceName", element.getAttribute("url"));
			properties.addPropertyValue("iface", element.getAttribute("interface"));
			properties.addPropertyValue("serialize", element.getAttribute("serialize"));
			properties.addPropertyValue("callMethod", element.getAttribute("callMethod"));
			properties.addPropertyValue("timeout", element.getAttribute("timeout"));
			properties.addPropertyValue("loadbalance", element.getAttribute("loadbalance"));
			properties.addPropertyValue("cluster", element.getAttribute("cluster"));
			properties.addPropertyValue("retries", element.getAttribute("retries"));
			properties.addPropertyValue("timeoutRetry", element.getAttribute("timeoutRetry"));
			properties.addPropertyValue("version", element.getAttribute("version"));

			String callback = element.getAttribute("callback");
			if (StringUtils.isNotEmpty(callback)) {
				if (!parserContext.getRegistry().containsBeanDefinition(callback)) {
					throw new IllegalStateException("callback reference must have a reference to callback bean");
				}
				properties.addPropertyValue("callback", new RuntimeBeanReference(callback));
			}

			try {
				if (element.hasAttribute("vip")) {
					properties.addPropertyValue("vip", resolveReference(element, "vip"));
				}
			} catch (RegistryException e) {
				logger.error("", e);
			}

		}

		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

	private static String resolveReference(Element element, String attribute) throws RegistryException {
		String value = element.getAttribute(attribute);
		if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
			String valueInCache = configManager.getProperty(value.substring(2, value.length() - 1));
			// RegistryCache.getInstance();
			// String valueInCache =
			// RegistryCache.getProperty(value.substring(2, value.length() -
			// 1));
			if (valueInCache == null) {
				throw new IllegalStateException("引用了properties中不存在的变量：" + element.getAttribute(attribute));
			} else {
				value = valueInCache;
			}
		}
		return value;
	}

}