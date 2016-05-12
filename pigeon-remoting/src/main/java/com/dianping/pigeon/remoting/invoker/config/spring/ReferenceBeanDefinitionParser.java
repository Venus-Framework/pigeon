/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.config.spring;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.InvokerBootStrap;
import com.dianping.pigeon.remoting.invoker.config.InvokerMethodConfig;
import com.dianping.pigeon.util.ClassUtils;

public class ReferenceBeanDefinitionParser implements BeanDefinitionParser {

	/** Default placeholder prefix: "${" */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
	/** Default placeholder suffix: "}" */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private static final Logger logger = LoggerLoader.getLogger(ReferenceBeanDefinitionParser.class);

	private final Class<?> beanClass;

	private final boolean required;

	public static AtomicInteger idCounter = new AtomicInteger();

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static boolean checkRefExists = configManager.getBooleanValue("pigeon.config.spring.checkrefexists", false);

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
		if (StringUtils.isBlank(id)) {
			id = "pigeonRef_" + idCounter.incrementAndGet();
		}
		beanDefinition.setBeanClass(ReferenceBean.class);
		beanDefinition.setInitMethodName("init");
		InvokerBootStrap.startup();

		MutablePropertyValues properties = beanDefinition.getPropertyValues();
		if (element.hasAttribute("interface")) {
			properties.addPropertyValue("interfaceName", resolveReference(element, "interface"));
		}
		if (element.hasAttribute("url")) {
			properties.addPropertyValue("url", resolveReference(element, "url"));
		} else if (element.hasAttribute("interface")) {
			properties.addPropertyValue("url", resolveReference(element, "interface"));
		}
		if (element.hasAttribute("serialize")) {
			properties.addPropertyValue("serialize", resolveReference(element, "serialize"));
		}
		if (element.hasAttribute("protocol")) {
			properties.addPropertyValue("protocol", resolveReference(element, "protocol"));
		}
		if (element.hasAttribute("callType")) {
			properties.addPropertyValue("callType", resolveReference(element, "callType"));
		}
		if (element.hasAttribute("timeout")) {
			properties.addPropertyValue("timeout", resolveReference(element, "timeout"));
		}
		if (element.hasAttribute("loadBalance")) {
			properties.addPropertyValue("loadBalance", resolveReference(element, "loadBalance"));
		}
		if (element.hasAttribute("loadBalanceClass")) {
			String clazz = resolveReference(element, "loadBalanceClass");
			if (StringUtils.isNotBlank(clazz)) {
				try {
					Class<?> cl = ClassUtils.loadClass(clazz);
					properties.addPropertyValue("loadBalanceClass", cl);
				} catch (ClassNotFoundException e) {
					logger.warn("invalid loadBalanceClass:" + clazz + ", caused by " + e.getMessage());
				}
			}
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
		if (element.hasAttribute("secret")) {
			properties.addPropertyValue("secret", resolveReference(element, "secret"));
		}
		String callback = element.getAttribute("callback");
		if (StringUtils.isNotEmpty(callback)) {
			if (checkRefExists && !parserContext.getRegistry().containsBeanDefinition(callback)) {
				throw new IllegalStateException("callback reference must have a reference to callback bean");
			}
			properties.addPropertyValue("callback", new RuntimeBeanReference(callback));
		}
		if (element.hasChildNodes()) {
			parseMethods(id, element.getChildNodes(), beanDefinition, parserContext);
		}
		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

	private static BeanDefinition parseMethod(Element element, ParserContext parserContext, Class<?> beanClass,
			boolean required) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setLazyInit(false);
		String id = element.getAttribute("id");
		if (StringUtils.isBlank(id)) {
			id = "pigeonService_" + idCounter.incrementAndGet();
		}
		beanDefinition.setBeanClass(beanClass);
		MutablePropertyValues properties = beanDefinition.getPropertyValues();
		if (element.hasAttribute("name")) {
			properties.addPropertyValue("name", resolveReference(element, "name"));
		}
		if (element.hasAttribute("timeout")) {
			properties.addPropertyValue("timeout", resolveReference(element, "timeout"));
		}
		if (element.hasAttribute("retries")) {
			properties.addPropertyValue("retries", resolveReference(element, "retries"));
		}
		if (element.hasAttribute("actives")) {
			properties.addPropertyValue("actives", resolveReference(element, "actives"));
		}
		if (element.hasAttribute("callType")) {
			properties.addPropertyValue("callType", resolveReference(element, "callType"));
		}
		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

	private static void parseMethods(String id, NodeList nodeList, RootBeanDefinition beanDefinition,
			ParserContext parserContext) {
		if (nodeList != null && nodeList.getLength() > 0) {
			ManagedList methods = null;
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node instanceof Element) {
					Element element = (Element) node;
					if ("method".equals(node.getNodeName()) || "method".equals(node.getLocalName())) {
						String methodName = element.getAttribute("name");
						if (methodName == null || methodName.length() == 0) {
							throw new IllegalStateException("<pigeon:method> name attribute == null");
						}
						if (methods == null) {
							methods = new ManagedList();
						}
						BeanDefinition methodBeanDefinition = parseMethod(((Element) node), parserContext,
								InvokerMethodConfig.class, false);
						String name = id + "." + methodName;
						BeanDefinitionHolder methodBeanDefinitionHolder = new BeanDefinitionHolder(
								methodBeanDefinition, name);
						methods.add(methodBeanDefinitionHolder);
					}
				}
			}
			if (methods != null) {
				beanDefinition.getPropertyValues().addPropertyValue("methods", methods);
			}
		}
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