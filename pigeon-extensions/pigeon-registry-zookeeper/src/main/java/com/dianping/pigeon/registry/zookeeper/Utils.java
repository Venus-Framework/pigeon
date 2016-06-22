package com.dianping.pigeon.registry.zookeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.util.Constants;

public class Utils {

	private static final Logger logger = LoggerLoader.getLogger(Utils.class);

	private static final ObjectMapper mapper = new ObjectMapper();

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

	public static String getHeartBeatPath(String serviceAddress) {
		String path = Constants.HEARTBEAT_PATH + Constants.PATH_SEPARATOR + serviceAddress;
		return path;
	}

	public static String getProtocolPath(String serviceAddress) {
		String path = Constants.PROTOCOL_PATH + Constants.PATH_SEPARATOR + serviceAddress;
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

	public static String getProtocolInfo(Map<String, Boolean> infoMap) throws JsonProcessingException {
		return mapper.writeValueAsString(infoMap);
	}

	public static Map<String, Boolean> getProtocolInfoMap(String info) throws IOException {

		if(StringUtils.isNotBlank(info)) {
			return mapper.readValue(info, ConcurrentHashMap.class);
		}

		return new ConcurrentHashMap<String, Boolean>();
	}
}
