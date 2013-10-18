package com.dianping.pigeon.config.spring;

import java.util.Map;

import org.springframework.beans.factory.xml.BeanDefinitionParser;

public interface BeanDefinitionParserLoader {

	Map<String, BeanDefinitionParser> loadBeanDefinitionParsers();
}
