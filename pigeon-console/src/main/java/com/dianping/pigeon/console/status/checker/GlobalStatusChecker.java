package com.dianping.pigeon.console.status.checker;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.service.Phase;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;
import com.dianping.pigeon.util.CollectionUtils;
import com.dianping.pigeon.util.VersionUtils;

public class GlobalStatusChecker {

	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static final StatusChecker providerStatusChecker = new ProviderStatusChecker();

	public static boolean isOnline() {
		boolean isOnline = true;
		Map<String, Integer> weights = ServiceProviderFactory.getServerWeight();
		if (!CollectionUtils.isEmpty(weights)) {
			for (Integer weight : weights.values()) {
				if (weight <= 0) {
					isOnline = false;
					break;
				}
			}
		} else {
			isOnline = false;
		}
		return isOnline;
	}

	public static Map<String, Object> getGlobalStatusProperties() {
		Map<String, Object> props = new LinkedHashMap<String, Object>();
		props.put("env", configManager.getEnv());
		props.put("swimlane", configManager.getGroup());
		props.put("version", VersionUtils.VERSION);
		props.put("phase", ServiceProviderFactory.getPhase().toString());
		props.put("online", isOnline());
		props.put("weight", ServiceProviderFactory.getServerWeight());
		props.put("error", "");

		String error = providerStatusChecker.checkError();
		if (!StringUtils.isBlank(error)) {
			props.put("error", error);
		}

		return props;
	}

}
