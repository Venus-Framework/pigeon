/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.util;


public final class Constants {

	public static final String CHARSET = "UTF-8";
	public static final String DP_PATH = "/DP";
	public static final String CONFIG_PATH = "/DP/CONFIG";
	public static final String CONFIG_TIMESTAMP = "TIMESTAMP";
	public static final String SERVICE_PATH = "/DP/SERVER";
	public static final String WEIGHT_PATH = "/DP/WEIGHT";
	public static final String REGISTRY_PATH = "/DP/REGISTRY";
	public static final String PATH_SEPARATOR = "/";
	public static final String PLACEHOLDER = "^";

	public static final String KEY_GROUP = "swimlane";
	public static final String DEFAULT_GROUP = "";
	public static final String KEY_WEIGHT = "weight";
	public static final String DEFAULT_WEIGHT = "1";
	public static final int MIN_WEIGHT = 0;
	public static final int MAX_WEIGHT = 100;
	public static final int DEFAULT_WEIGHT_INT = Integer.parseInt(DEFAULT_WEIGHT);
	public static final String KEY_AUTO_REGISTER = "auto.register";
	public static final String DEFAULT_AUTO_REGISTER = "true";
	public static final boolean DEFAULT_AUTO_REGISTER_BOOL = Boolean.parseBoolean(DEFAULT_AUTO_REGISTER);
	public static final String KEY_LOCAL_IP = "local.ip";
	
	public static final String KEY_REGISTRY_ADDRESS = "pigeon.registry.address";
	public static final String KEY_REGISTRY_TYPE = "pigeon.registry.type";
	public static final String REGISTRY_TYPE_ZOOKEEPER = "zookeeper";
	public static final String REGISTRY_TYPE_LOCAL = "local";
	public static final String DEFAULT_REGISTRY_TYPE = REGISTRY_TYPE_ZOOKEEPER;
}
