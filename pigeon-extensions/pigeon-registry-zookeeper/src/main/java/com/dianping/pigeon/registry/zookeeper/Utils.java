package com.dianping.pigeon.registry.zookeeper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.log.LoggerLoader;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.registry.util.Constants;

public class Utils {

	private static final Logger logger = LoggerLoader.getLogger(Utils.class);

	public static String unescapeServiceName(String serviceName) {
		return serviceName.replace(Constants.PLACEHOLDER, Constants.PATH_SEPARATOR);
	}

	public static String escapeServiceName(String serviceName) {
		return serviceName.replace(Constants.PATH_SEPARATOR, Constants.PLACEHOLDER);
	}

	public static String getServicePath(String serviceName, String group) {
		String path = Constants.SERVICE_PATH + Constants.PATH_SEPARATOR + escapeServiceName(serviceName);
		if (!StringUtils.isBlank(group)) {
			path = path + Constants.PATH_SEPARATOR + group;
		}
		return path;
	}

	public static String getWeightPath(String serviceAddress) {
		String path = Constants.WEIGHT_PATH + Constants.PATH_SEPARATOR + serviceAddress;
		return path;
	}

	public static String getAppPath(String serviceAddress) {
		String path = Constants.APP_PATH + Constants.PATH_SEPARATOR + serviceAddress;
		return path;
	}
	
	public static String getVersionPath(String serviceAddress) {
		String path = Constants.VERSION_PATH + Constants.PATH_SEPARATOR + serviceAddress;
		return path;
	}

	public static String normalizeGroup(String group) {
		return StringUtils.isBlank(group) ? Constants.DEFAULT_GROUP : group;
	}

	public static List<String[]> getServiceIpPortList(String serviceAddress) {
		List<String[]> result = new ArrayList<String[]>();
		if (serviceAddress != null && serviceAddress.length() > 0) {
			String[] hostArray = serviceAddress.split(",");
			for (String host : hostArray) {
				int idx = host.lastIndexOf(":");
				if (idx != -1) {
					String ip = null;
					int port = -1;
					try {
						ip = host.substring(0, idx);
						port = Integer.parseInt(host.substring(idx + 1));
					} catch (RuntimeException e) {
						logger.warn("invalid host: " + host + ", ignored!");
					}
					if (ip != null && port > 0) {
						result.add(new String[] { ip, port + "" });
					}
				} else {
					logger.warn("invalid host: " + host + ", ignored!");
				}
			}
		}
		return result;
	}

	public static String getServicePath(String path) {
		int idx = path.lastIndexOf(":");
		if (idx != -1) {
			String str = path.substring(idx + 1);
			try {
				Integer.parseInt(str);
				int idx2 = path.lastIndexOf("/");
				return path.substring(0, idx2);
			} catch (NumberFormatException e) {
				return path;
			}
		} else {
			return path;
		}
	}

	public static boolean isValidAddress(String addr) {
		if (StringUtils.isNotBlank(addr) && addr.indexOf(":") != -1 && addr.length() > 10) {
			return true;
		}
		return false;
	}
}
