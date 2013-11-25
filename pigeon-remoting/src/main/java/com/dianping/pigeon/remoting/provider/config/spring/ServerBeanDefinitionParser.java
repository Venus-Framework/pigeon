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

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.provider.ServerFactory;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: PigeonBeanDefinitionParser.java, v 0.1 2013-6-24 下午9:58:37
 *          jianhuihuang Exp $
 */
public class ServerBeanDefinitionParser implements BeanDefinitionParser {

	/** Default placeholder prefix: "${" */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
	/** Default placeholder suffix: "}" */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private static final Logger logger = LoggerLoader.getLogger(ServerBeanDefinitionParser.class);

	private final Class<?> beanClass;

	private final boolean required;

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public ServerBeanDefinitionParser(Class<?> beanClass, boolean required) {
		this.beanClass = beanClass;
		this.required = required;
	}

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		return parse(element, parserContext, beanClass, required);
	}

	private static BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass,
			boolean required) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setLazyInit(false);
		String id = element.getAttribute("id");
		beanDefinition.setBeanClass(ServerBean.class);
		beanDefinition.setInitMethodName("init");

		MutablePropertyValues properties = beanDefinition.getPropertyValues();
		if (element.hasAttribute("group")) {
			properties.addPropertyValue("group", resolveReference(element, "group"));
		}
		if (element.hasAttribute("port")) {
			properties.addPropertyValue("port", resolveReference(element, "port"));
		}
		if (element.hasAttribute("corePoolSize")) {
			properties.addPropertyValue("corePoolSize", resolveReference(element, "corePoolSize"));
		}
		if (element.hasAttribute("maxPoolSize")) {
			properties.addPropertyValue("maxPoolSize", resolveReference(element, "maxPoolSize"));
		}
		if (element.hasAttribute("workQueueSize")) {
			properties.addPropertyValue("workQueueSize", resolveReference(element, "workQueueSize"));
		}
		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

	private static String resolveReference(Element element, String attribute) {
		String value = element.getAttribute(attribute);
		if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
			String valueInCache = configManager.getProperty(value.substring(2, value.length() - 1));
			if (valueInCache == null) {
				throw new IllegalStateException("引用了properties中不存在的变量：" + element.getAttribute(attribute));
			} else {
				value = valueInCache;
			}
		}
		return value;
	}
}