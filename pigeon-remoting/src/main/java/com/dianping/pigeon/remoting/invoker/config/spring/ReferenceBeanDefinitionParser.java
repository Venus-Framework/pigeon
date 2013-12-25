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
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.InvokerBootStrap;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: PigeonBeanDefinitionParser.java, v 0.1 2013-6-24 下午9:58:37
 *          jianhuihuang Exp $
 */
public class ReferenceBeanDefinitionParser implements BeanDefinitionParser {

	/** Default placeholder prefix: "${" */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
	/** Default placeholder suffix: "}" */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private static final Logger logger = LoggerLoader.getLogger(ReferenceBeanDefinitionParser.class);

	private final Class<?> beanClass;

	private final boolean required;

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public ReferenceBeanDefinitionParser(Class<?> beanClass, boolean required) {
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

		beanDefinition.setBeanClass(ProxyBeanFactory.class);
		beanDefinition.setInitMethodName("init");
		InvokerBootStrap.startup();

		MutablePropertyValues properties = beanDefinition.getPropertyValues();
		if (element.hasAttribute("url")) {
			properties.addPropertyValue("serviceName", resolveReference(element, "url"));
		}
		if (element.hasAttribute("interface")) {
			properties.addPropertyValue("iface", resolveReference(element, "interface"));
		}
		if (element.hasAttribute("serialize")) {
			properties.addPropertyValue("serialize", resolveReference(element, "serialize"));
		}
		if (element.hasAttribute("protocol")) {
			properties.addPropertyValue("protocol", resolveReference(element, "protocol"));
		}
		if (element.hasAttribute("callType")) {
			properties.addPropertyValue("callMethod", resolveReference(element, "callType"));
		}
		if (element.hasAttribute("timeout")) {
			properties.addPropertyValue("timeout", resolveReference(element, "timeout"));
		}
		if (element.hasAttribute("loadbalance")) {
			properties.addPropertyValue("loadbalance", resolveReference(element, "loadbalance"));
		}
		if (element.hasAttribute("cluster")) {
			properties.addPropertyValue("cluster", resolveReference(element, "cluster"));
		}
		if (element.hasAttribute("retries")) {
			properties.addPropertyValue("retries", resolveReference(element, "retries"));
		}
		if (element.hasAttribute("timeoutRetry")) {
			properties.addPropertyValue("timeoutRetry", resolveReference(element, "timeoutRetry"));
		}
		if (element.hasAttribute("version")) {
			properties.addPropertyValue("version", resolveReference(element, "version"));
		}
		if (element.hasAttribute("vip")) {
			properties.addPropertyValue("vip", resolveReference(element, "vip"));
		}
		String callback = element.getAttribute("callback");
		if (StringUtils.isNotEmpty(callback)) {
			if (!parserContext.getRegistry().containsBeanDefinition(callback)) {
				throw new IllegalStateException("callback reference must have a reference to callback bean");
			}
			properties.addPropertyValue("callback", new RuntimeBeanReference(callback));
		}

		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

	private static String resolveReference(Element element, String attribute) {
		String value = element.getAttribute(attribute);
		if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
			String valueInCache = configManager.getStringValue(value.substring(2, value.length() - 1));
			if (valueInCache == null) {
				throw new IllegalStateException("引用了properties中不存在的变量：" + element.getAttribute(attribute));
			} else {
				value = valueInCache;
			}
		}
		return value;
	}

}