package com.dianping.pigeon.remoting.common.util;

import java.util.List;

import org.springframework.util.CollectionUtils;

import com.dianping.pigeon.config.ConfigManagerLoader;

public class ServiceConfigUtils {

	private static String interfacePackagesConfig = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.provider.interface.packages", "com.dianping,com.dp");

	private static String[] interfacePackages = new String[] { "com.dianping" };

	static {
		interfacePackages = (interfacePackagesConfig == null || interfacePackagesConfig.length() == 0) ? null
				: Constants.COMMA_SPLIT_PATTERN.split(interfacePackagesConfig);
	}

	private static boolean isValidType(Class type) {
		String beanClassName = type.getName();
		for (String pkg : interfacePackages) {
			if (beanClassName.startsWith(pkg)) {
				return true;
			}
		}
		return false;
	}

	public static <T> Class<?> getServiceInterface(Class<?> type) {
		Class<?>[] interfaces = type.getInterfaces();
		Class<?> interfaceClass = null;
		if (interfaces != null && interfaces.length > 0) {
			interfaceClass = type.getInterfaces()[0];
		} else {
			List<Class<?>> allInterfaces = org.apache.commons.lang.ClassUtils.getAllInterfaces(type);
			if (!CollectionUtils.isEmpty(allInterfaces)) {
				for (Class<?> i : allInterfaces) {
					if (isValidType(i)) {
						interfaceClass = i;
						break;
					}
				}
				if (interfaceClass == null) {
					interfaceClass = allInterfaces.get(0);
				}
			}
			if (interfaceClass == null) {
				interfaceClass = type;
			}
		}
		return interfaceClass;
	}
}
