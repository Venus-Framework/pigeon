/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config.spring;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.dianping.pigeon.monitor.Log4jLoader;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: PigeonBeanDefinitionParser.java, v 0.1 2013-6-24 下午9:58:37
 *          jianhuihuang Exp $
 */
public class CommonBeanDefinitionParser implements BeanDefinitionParser {

	/** Default placeholder prefix: "${" */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
	/** Default placeholder suffix: "}" */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private static final Logger logger = Log4jLoader.getLogger(CommonBeanDefinitionParser.class);

	private final Class<?> beanClass;

	private final boolean required;

	public CommonBeanDefinitionParser(Class<?> beanClass, boolean required) {
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

//		if (beanClass == ExtensionBean.class) {
//			beanDefinition.setBeanClass(ExtensionBean.class);
//			beanDefinition.setInitMethodName("init");
//
//			MutablePropertyValues properties = beanDefinition.getPropertyValues();
//			properties.addPropertyValue("point", element.getAttribute("point"));
//			String bean = element.getAttribute("bean");
//			if (StringUtils.isEmpty(bean) || !parserContext.getRegistry().containsBeanDefinition(bean)) {
//				throw new IllegalStateException("extension must have a reference to bean");
//			}
//			properties.addPropertyValue("bean", new RuntimeBeanReference(bean));
//
//			Element contentElement = DomUtils.getChildElementByTagName(element, "content");
//			properties.addPropertyValue("content", contentElement);
//		}

		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

}