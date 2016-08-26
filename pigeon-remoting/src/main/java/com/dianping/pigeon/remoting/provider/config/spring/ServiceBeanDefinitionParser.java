/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config.spring;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.Logger;
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
import com.dianping.pigeon.remoting.provider.config.ProviderMethodConfig;
import com.dianping.pigeon.remoting.provider.process.threadpool.RequestThreadPoolProcessor;

public class ServiceBeanDefinitionParser implements BeanDefinitionParser {

	/** Default placeholder prefix: "${" */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
	/** Default placeholder suffix: "}" */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private static final Logger logger = LoggerLoader.getLogger(ServiceBeanDefinitionParser.class);

	private final Class<?> beanClass;

	private final boolean required;

	public static AtomicInteger idCounter = new AtomicInteger();

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static boolean checkRefExists = configManager.getBooleanValue("pigeon.config.spring.checkrefexists", false);

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
		beanDefinition.setBeanClass(SingleServiceBean.class);
		beanDefinition.setInitMethodName("init");

		MutablePropertyValues properties = beanDefinition.getPropertyValues();
		String ref = element.getAttribute("ref");
		if (checkRefExists && !parserContext.getRegistry().containsBeanDefinition(ref)) {
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

		String url = null;
		if (element.hasAttribute("url")) {
			url = resolveReference(element, "url");
			properties.addPropertyValue("url", url);
		}
		if (element.hasAttribute("interface")) {
			String interfaceName = resolveReference(element, "interface");
			if (StringUtils.isBlank(url)) {
				url = interfaceName;
			}
			properties.addPropertyValue("interfaceName", interfaceName);
		}
		if (element.hasAttribute("version")) {
			properties.addPropertyValue("version", resolveReference(element, "version"));
		}
		if (element.hasAttribute("cancelTimeout")) {
			properties.addPropertyValue("cancelTimeout", resolveReference(element, "cancelTimeout"));
		}
		if (element.hasAttribute("useSharedPool")) {
			properties.addPropertyValue("useSharedPool", resolveReference(element, "useSharedPool"));
		}
		if (element.hasAttribute("actives")) {
			properties.addPropertyValue("actives", resolveReference(element, "actives"));
		}
		if (element.hasChildNodes()) {
			parseMethods(url, id, element.getChildNodes(), beanDefinition, parserContext);
		}
		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

	private static BeanDefinition parseMethod(String url, String methodName, Element element,
			ParserContext parserContext, Class<?> beanClass, boolean required) {
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
		if (element.hasAttribute("actives")) {
			properties.addPropertyValue("actives", resolveReference(element, "actives"));
			String value = element.getAttribute("actives");
			if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
				RequestThreadPoolProcessor.methodPoolConfigKeys.put(url + "#" + methodName,
						value.substring(2, value.length() - 1));
			}
		}
		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

	private static void parseMethods(String url, String id, NodeList nodeList, RootBeanDefinition beanDefinition,
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
						BeanDefinition methodBeanDefinition = parseMethod(url, methodName, ((Element) node),
								parserContext, ProviderMethodConfig.class, false);
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