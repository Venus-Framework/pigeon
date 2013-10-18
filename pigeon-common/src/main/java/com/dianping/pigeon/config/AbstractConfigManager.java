/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public abstract class AbstractConfigManager implements ConfigManager {

	@Override
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value != null ? value : defaultValue;
	}

}
