package com.dianping.pigeon.registry.zookeeper;

import java.util.Iterator;

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
		if (!isEmpty(group)) {
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
		if (!isEmpty(key)) {
			path = path + Constants.PATH_SEPARATOR + key;
		}
		return path;
	}

	public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
	
	public static String join(Iterator iterator, String separator) {
		// handle null, zero and one elements before building a buffer
		if (iterator == null) {
			return null;
		}
		if (!iterator.hasNext()) {
			return "";
		}
		Object first = iterator.next();
		if (!iterator.hasNext()) {
			return first == null ? "" : first.toString();
		}

		// two or more elements
		StringBuffer buf = new StringBuffer(256); // Java default is 16,
													// probably too small
		if (first != null) {
			buf.append(first);
		}

		while (iterator.hasNext()) {
			if (separator != null) {
				buf.append(separator);
			}
			Object obj = iterator.next();
			if (obj != null) {
				buf.append(obj);
			}
		}
		return buf.toString();
	}
}
