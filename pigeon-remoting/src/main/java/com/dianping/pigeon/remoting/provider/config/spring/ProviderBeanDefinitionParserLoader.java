/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.xml.BeanDefinitionParser;

import com.dianping.pigeon.config.spring.BeanDefinitionParserLoader;
import com.dianping.pigeon.remoting.provider.config.ServiceBean;

/**
 * 
 * @author xiangwu
 */
public class ProviderBeanDefinitionParserLoader implements BeanDefinitionParserLoader {

	@Override
	public Map<String, BeanDefinitionParser> loadBeanDefinitionParsers() {
		Map<String, BeanDefinitionParser> parsers = new HashMap<String, BeanDefinitionParser>();
		parsers.put("service", new ProviderBeanDefinitionParser(ServiceBean.class, true));
		return parsers;
	}

}