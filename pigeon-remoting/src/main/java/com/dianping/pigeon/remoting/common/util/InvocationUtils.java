/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.util;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.site.helper.Splitters;
import com.site.helper.Stringizers;

public class InvocationUtils {

	private static ConcurrentHashMap<String, String> remoteCallNameCache = new ConcurrentHashMap<String, String>();

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static final int defaultStrMaxLength = configManager.getIntValue(Constants.KEY_STRING_MAXLENGTH,
			Constants.DEFAULT_STRING_MAXLENGTH);

	private static final int defaultStrMaxItems = configManager.getIntValue(Constants.KEY_STRING_MAXITEMS,
			Constants.DEFAULT_STRING_MAXITEMS);

	public static String toJsonString(Object obj) {
		return Stringizers.forJson().from(obj, defaultStrMaxLength, defaultStrMaxItems);
	}

	public static String toJsonString(Object obj, int strMaxLength, int strMaxItems) {
		return Stringizers.forJson().from(obj, strMaxLength, strMaxItems);
	}

	public static String getRemoteCallFullName(String serviceName, String methodName, Class<?>[] parameterTypes) {
		if (parameterTypes != null) {
			String[] parameterTypes_ = new String[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				parameterTypes_[i] = parameterTypes[i].getSimpleName();
			}
			return getRemoteCallFullName(serviceName, methodName, parameterTypes_);
		} else {
			return getRemoteCallFullName(serviceName, methodName, new String[0]);
		}
	}

	public static String getRemoteCallFullName(String serviceName, String methodName, String[] parameterTypes) {
		String cacheKey = new StringBuilder(serviceName).append("#").append(methodName).append("#")
				.append(StringUtils.join(parameterTypes, "#")).toString();
		String name = remoteCallNameCache.get(cacheKey);
		if (name == null) {
			List<String> serviceFrags = Splitters.by("/").noEmptyItem().split(serviceName);
			int fragLenght = serviceFrags.size();
			name = "Unknown";
			if (fragLenght > 2) {
				StringBuilder sb = new StringBuilder(128);
				sb.append(serviceFrags.get(fragLenght - 2)).append(':').append(serviceFrags.get(fragLenght - 1))
						.append(':').append(methodName);
				sb.append('(');
				int pLen = parameterTypes.length;
				for (int i = 0; i < pLen; i++) {
					String parameter = parameterTypes[i];
					int idx = parameter.lastIndexOf(".");
					if (idx > -1) {
						parameter = parameter.substring(idx + 1);
					}
					sb.append(parameter);
					if (i < pLen - 1) {
						sb.append(',');
					}
				}
				sb.append(')');
				name = sb.toString();
			}
			remoteCallNameCache.putIfAbsent(cacheKey, name);
		}
		return name;
	}

}
