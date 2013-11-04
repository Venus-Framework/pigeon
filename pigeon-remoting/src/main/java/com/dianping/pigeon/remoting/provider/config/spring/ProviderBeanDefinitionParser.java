/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config.spring;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.dianping.pigeon.component.QueryString;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.provider.ServerFactory;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: PigeonBeanDefinitionParser.java, v 0.1 2013-6-24 下午9:58:37
 *          jianhuihuang Exp $
 */
public class ProviderBeanDefinitionParser implements BeanDefinitionParser {

	/** Default placeholder prefix: "${" */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
	/** Default placeholder suffix: "}" */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private static final Logger logger = Logger.getLogger(ProviderBeanDefinitionParser.class);

	private final Class<?> beanClass;

	private final boolean required;

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public ProviderBeanDefinitionParser(Class<?> beanClass, boolean required) {
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

		if (beanClass == ServiceBean.class) {
			beanDefinition.setBeanClass(NewServiceRegistry.class);
			beanDefinition.setInitMethodName("init");

			String ref = element.getAttribute("ref");
			if (!parserContext.getRegistry().containsBeanDefinition(ref)) {
				throw new IllegalStateException("service must have a reference to impl bean");
			}
			MutablePropertyValues properties = beanDefinition.getPropertyValues();
			properties.addPropertyValue("serviceName", getServiceNameWithZoneAndGroup(element));
			properties.addPropertyValue("serviceImpl", new RuntimeBeanReference(ref));
			properties.addPropertyValue("port", getPort(element));
		}

		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

	private static int getPort(Element element) {
		try {
			String port = resolveReference(element, "port");
			if (!StringUtils.isBlank(port)) {
				return Integer.parseInt(port);
			} else {
				return ServerFactory.DEFAULT_PORT;
			}
		} catch (Exception e) {
			logger.error("", e);
			throw new RuntimeException("", e);
		}
	}

	private static String getServiceNameWithZoneAndGroup(Element element) {
		String serviceName = element.getAttribute(element.hasAttribute("name") ? "name" : "interface");
//		try {
//			QueryString parameters = new QueryString();
//			RegistryManager.getInstance();
//			String zone = RegistryManager.getProperty("zoneName");
//			if (zone != null) {
//				parameters.addParameter("zone", zone);
//			}
//			if (element.hasAttribute("group")) {
//				String group = resolveReference(element, "group");
//				parameters.addParameter("group", group);
//			}
//			if (!parameters.isEmpty()) {
//				serviceName += QueryString.PREFIX + parameters;
//			}
//		} catch (Exception e) {
//			logger.error("", e);
//			throw new RuntimeException("", e);
//		}
		return serviceName;
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