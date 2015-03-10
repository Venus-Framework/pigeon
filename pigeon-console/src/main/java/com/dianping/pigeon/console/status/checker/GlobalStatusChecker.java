package com.dianping.pigeon.console.status.checker;

import java.util.Map;

import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;
import com.dianping.pigeon.util.CollectionUtils;

public class GlobalStatusChecker {

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

	public static boolean isInitialized() {
		return RegistryManager.isInitialized();
	}

}
