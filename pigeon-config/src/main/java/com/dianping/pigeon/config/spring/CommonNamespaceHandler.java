/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config.spring;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.dianping.pigeon.extension.ExtensionLoader;

public class CommonNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		List<BeanDefinitionParserLoader> loaders = ExtensionLoader.getExtensionList(BeanDefinitionParserLoader.class);
		if (loaders != null) {
			for (BeanDefinitionParserLoader loader : loaders) {
				Map<String, BeanDefinitionParser> parsers = loader.loadBeanDefinitionParsers();
				if (parsers != null) {
					for (String key : parsers.keySet()) {
						registerBeanDefinitionParser(key, parsers.get(key));
					}
				}
			}
		}
	}

}