/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config.spring;

import java.util.concurrent.atomic.AtomicInteger;

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
import com.dianping.pigeon.log.LoggerLoader;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: PigeonBeanDefinitionParser.java, v 0.1 2013-6-24 下午9:58:37
 *          jianhuihuang Exp $
 */
public class ServiceBeanDefinitionParser implements BeanDefinitionParser {

	/** Default placeholder prefix: "${" */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
	/** Default placeholder suffix: "}" */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private static final Logger logger = LoggerLoader.getLogger(ServiceBeanDefinitionParser.class);

	private final Class<?> beanClass;

	private final boolean required;
	
	public static AtomicInteger idCounter = new AtomicInteger();

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public ServiceBeanDefinitionParser(Class<?> beanClass, boolean required) {
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
			id = "pigeonService_" + idCounter.incrementAndGet();
		}
		beanDefinition.setBeanClass(ServiceBean.class);
		beanDefinition.setInitMethodName("init");

		MutablePropertyValues properties = beanDefinition.getPropertyValues();
		String ref = element.getAttribute("ref");
		if (!parserContext.getRegistry().containsBeanDefinition(ref)) {
			throw new IllegalStateException("service must have a reference to bean:" + ref);
		}
		properties.addPropertyValue("serviceImpl", new RuntimeBeanReference(ref));

		if (element.hasAttribute("server")) {
			String server = element.getAttribute("server");
			if (!parserContext.getRegistry().containsBeanDefinition(server)) {
				throw new IllegalStateException("service must have a reference to bean:" + server);
			}
			properties.addPropertyValue("serverBean", new RuntimeBeanReference(server));
		}
		
		if (element.hasAttribute("url")) {
			properties.addPropertyValue("url", resolveReference(element, "url"));
		}
		if (element.hasAttribute("interface")) {
			properties.addPropertyValue("interfaceName", resolveReference(element, "interface"));
		}
		if (element.hasAttribute("version")) {
			properties.addPropertyValue("version", resolveReference(element, "version"));
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