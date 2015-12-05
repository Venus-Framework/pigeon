package com.dianping.pigeon.console;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;

public class Utils {

	private static final List<String> LOCAL_IP_LIST = new ArrayList<String>();
	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static final String DEFAULT_SIGN = "WQMlgikuPfCFNE8=ZEhN2k8xxJMu";
	private static final String SIGN = configManager.getStringValue("pigeon.console.sign", DEFAULT_SIGN);

	static {
		LOCAL_IP_LIST.add("127.0.0.1");
		LOCAL_IP_LIST.add("0:0:0:0:0:0:0:1");
	}

	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public static boolean isGranted(HttpServletRequest request) {
		boolean isGranted = false;
		String ip = Utils.getIpAddr(request);
		String sign = request.getParameter("sign");
		isGranted = LOCAL_IP_LIST.contains(ip) || ip.equals(configManager.getLocalIp());
		if (!isGranted && StringUtils.isNotBlank(sign)) {
			isGranted = sign.equals(SIGN);
		}
		return isGranted;
	}

	public static String getSign() {
		return SIGN;
	}
}
