package com.dianping.pigeon.registry.zookeeper;

import com.dianping.pigeon.registry.util.Constants;

public class Utils {

	public static boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}

	public static String unescapeServiceName(String serviceName) {
		return serviceName.replace(Constants.PLACEHOLDER, Constants.PATH_SEPARATOR);
	}

	public static String escapeServiceName(String serviceName) {
		return serviceName.replace(Constants.PATH_SEPARATOR, Constants.PLACEHOLDER);
	}
	
	public static String getServicePath(String serviceName, String group) {
		String path = Constants.SERVICE_PATH + Constants.PATH_SEPARATOR + escapeServiceName(serviceName);
		if(!isEmpty(group)) {
			path = path + Constants.PATH_SEPARATOR + group;
		}
		return path;
	}

	public static String getWeightPath(String serviceAddress) {
		String path = Constants.WEIGHT_PATH + Constants.PATH_SEPARATOR + serviceAddress;
		return path;
	}
	
	public static String normalizeGroup(String group) {
		return group == null ? Constants.DEFAULT_GROUP : group;	
	}

	public static String getRegistryPath(String serviceAddress) {
		String path = Constants.REGISTRY_PATH + Constants.PATH_SEPARATOR + serviceAddress;
		return path;
	}
	
	public static String getRegistryPath(String serviceAddress, String key) {
		String path = Constants.REGISTRY_PATH + Constants.PATH_SEPARATOR + serviceAddress;
		if(!isEmpty(key)) {
			path = path + Constants.PATH_SEPARATOR + key;
		}
		return path;
	}
}
