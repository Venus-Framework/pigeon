/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * 
 * @author xiangwu
 * @Sep 11, 2013
 * 
 */
public final class ExtensionLoader {

	private static final Logger logger = Logger
			.getLogger(ExtensionLoader.class);

	private static Map<Class<?>, Object> extensionMap = new ConcurrentHashMap<Class<?>, Object>();

	private static Map<Class<?>, List<?>> extensionListMap = new ConcurrentHashMap<Class<?>, List<?>>();

	public static <T> T getExtension(Class<T> clazz) {
		T extension = (T) extensionMap.get(clazz);
		if (extension == null) {
			ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
			for (T service : serviceLoader) {
				extensionMap.put(clazz, service);
				return service;
			}
			logger.warn("no extension found for class:" + clazz.getName());
		}
		return extension;
	}

	public static <T> List<T> getExtensionList(Class<T> clazz) {
		List<T> extensions = (List<T>) extensionListMap.get(clazz);
		if (extensions == null) {
			ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
			extensions = new ArrayList<T>();
			for (T service : serviceLoader) {
				extensions.add(service);
			}
			if (!extensions.isEmpty()) {
				extensionListMap.put(clazz, extensions);
			} else {
				logger.warn("no extension found for class:" + clazz.getName());
			}
		}
		return extensions;
	}

}
