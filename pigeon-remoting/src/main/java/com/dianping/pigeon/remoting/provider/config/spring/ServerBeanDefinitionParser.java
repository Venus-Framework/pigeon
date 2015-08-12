/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config.spring;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.log.LoggerLoader;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.provider.process.threadpool.RequestThreadPoolProcessor;

public class ServerBeanDefinitionParser implements BeanDefinitionParser {

	/** Default placeholder prefix: "${" */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
	/** Default placeholder suffix: "}" */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private static final Logger logger = LoggerLoader.getLogger(ServerBeanDefinitionParser.class);

	private final Class<?> beanClass;

	private final boolean required;

	public static AtomicInteger idCounter = new AtomicInteger();

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
		if (StringUtils.isBlank(id)) {
			id = "pigeonServer_" + idCounter.incrementAndGet();
		}
		beanDefinition.setBeanClass(ServerBean.class);
		beanDefinition.setInitMethodName("init");

		MutablePropertyValues properties = beanDefinition.getPropertyValues();
		if (element.hasAttribute("group")) {
			properties.addPropertyValue("group", resolveReference(element, "group"));
		}
		if (element.hasAttribute("port")) {
			properties.addPropertyValue("port", resolveReference(element, "port"));
		}
		if (element.hasAttribute("autoSelectPort")) {
			properties.addPropertyValue("autoSelectPort", resolveReference(element, "autoSelectPort"));
		}
		if (element.hasAttribute("corePoolSize")) {
			properties.addPropertyValue("corePoolSize", resolveReference(element, "corePoolSize"));
			String value = element.getAttribute("corePoolSize");
			if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
				RequestThreadPoolProcessor.sharedPoolCoreSizeKey = value.substring(2, value.length() - 1);
			}
		}
		if (element.hasAttribute("maxPoolSize")) {
			properties.addPropertyValue("maxPoolSize", resolveReference(element, "maxPoolSize"));
			String value = element.getAttribute("maxPoolSize");
			if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
				RequestThreadPoolProcessor.sharedPoolMaxSizeKey = value.substring(2, value.length() - 1);
			}
		}
		if (element.hasAttribute("workQueueSize")) {
			properties.addPropertyValue("workQueueSize", resolveReference(element, "workQueueSize"));
			String value = element.getAttribute("workQueueSize");
			if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
				RequestThreadPoolProcessor.sharedPoolQueueSizeKey = value.substring(2, value.length() - 1);
			}
		}
		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

	private static String resolveReference(Element element, String attribute) {
		String value = element.getAttribute(attribute);
		if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
			String valueInCache = configManager.getStringValue(value.substring(2, value.length() - 1));
			if (valueInCache == null) {
				throw new IllegalStateException("undefined config property:" + element.getAttribute(attribute));
			} else {
				value = valueInCache;
			}
		}
		return value;
	}
}