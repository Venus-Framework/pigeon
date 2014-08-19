package com.dianping.pigeon.registry.zookeeper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.registry.util.Constants;

public class Utils {

	private static final Logger logger = Logger.getLogger(Utils.class);

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

	public static String normalizeGroup(String group) {
		return StringUtils.isBlank(group) ? Constants.DEFAULT_GROUP : group;
	}

	public static String getEphemeralServicePath(String serviceName, String group) {
		StringBuilder sb = new StringBuilder(Constants.EPHEMERAL_SERVICE_PATH);
		sb.append(Constants.PATH_SEPARATOR).append(escapeServiceName(serviceName));
		if (!StringUtils.isBlank(group)) {
			sb.append('@').append(group);
		}
		return sb.toString();
	}

	public static String getEphemeralServicePath(String serviceName, String group, String serviceAddress) {
		StringBuilder sb = new StringBuilder(Constants.EPHEMERAL_SERVICE_PATH);
		sb.append(Constants.PATH_SEPARATOR).append(escapeServiceName(serviceName));
		if (!StringUtils.isBlank(group)) {
			sb.append('@').append(group);
		}
		sb.append(Constants.PATH_SEPARATOR).append(serviceAddress);
		return sb.toString();
	}

	public static List<String[]> getServiceIpPortList(String serviceAddress) {
		List<String[]> result = new ArrayList<String[]>();
		if (serviceAddress != null && serviceAddress.length() > 0) {
			String[] hostArray = serviceAddress.split(",");
			for (String host : hostArray) {
				String[] ipPort = host.split(":");
				if (ipPort.length != 2) {
					logger.error("****** invalid host: " + ipPort + ", ignored!");
				}
				result.add(ipPort);
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
