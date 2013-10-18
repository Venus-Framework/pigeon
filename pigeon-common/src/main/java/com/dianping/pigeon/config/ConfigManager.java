/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config;

import java.util.Properties;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public interface ConfigManager {

	public String getEnv();

	public String getAddress();

	public String getProperty(String key);

	public String getProperty(String key, String defaultValue);

	public void init(Properties properties);

}
