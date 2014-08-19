package com.dianping.pigeon.remoting.common.util;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;

public class SecurityUtils {

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static final String registryBlackList = configManager.getStringValue("pigeon.registry.blacklist", "");

	private static final String registryWhiteList = configManager.getStringValue("pigeon.registry.whitelist", "");

	public static boolean canRegister(String ip) {
		String[] whiteArray = registryWhiteList.split(",");
		for (String addr : whiteArray) {
			if (StringUtils.isBlank(addr)) {
				continue;
			}
			if (ip.startsWith(addr)) {
				return true;
			}
		}
		String[] blackArray = registryBlackList.split(",");
		for (String addr : blackArray) {
			if (StringUtils.isBlank(addr)) {
				continue;
			}
			if (ip.startsWith(addr)) {
				return false;
			}
		}
		return true;
	}
}
