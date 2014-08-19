package com.dianping.pigeon.console.status.checker;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dianping.phoenix.status.ComponentStatus.State;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.status.Phase;
import com.dianping.pigeon.remoting.common.status.StatusContainer;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;
import com.dianping.pigeon.util.CollectionUtils;
import com.dianping.pigeon.util.VersionUtils;

public class GlobalStatusChecker {

	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static final StatusChecker providerStatusChecker = new ProviderStatusChecker();

	public static State getState() {
		Map<String, Object> properties = getGlobalStatusProperties();
		String error = (String) properties.get("error");
		if (StringUtils.isNotBlank(error)) {
			return State.FAILED;
		}
		if (!GlobalStatusChecker.isInitialized()) {
			return State.INITIALIZING;
		} else {
			String phase = (String) properties.get("phase");
			if (phase.equals(Phase.TOUNPUBLISH.toString()) || phase.equals(Phase.UNPUBLISHED.toString())
					|| phase.equals(Phase.OFFLINE.toString())) {
				return State.MARKED_DOWN;
			} else if (phase.equals(Phase.PUBLISHING.toString()) || phase.equals(Phase.TOPUBLISH.toString())) {
				return State.INITIALIZING;
			} else {
				if (phase.equals(Phase.INVOKER_READY.toString())) {
					Map weightMap = (Map) properties.get("weight");
					if (weightMap.isEmpty()) {// client-side
						return State.INITIALIZED;
					} else {
						return State.INITIALIZING;
					}
				}
				return State.INITIALIZED;
			}
		}
	}

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

	public static Map<String, Object> getGlobalStatusProperties() {
		Map<String, Object> props = new LinkedHashMap<String, Object>();
		props.put("version", VersionUtils.VERSION);
		try {
			props.put("env", configManager.getEnv());
		} catch (Throwable e) {
			props.put("error", e.getMessage());
		}
		try {
			props.put("swimlane", configManager.getGroup());
		} catch (Throwable e) {
			props.put("error", e.getMessage());
		}
		props.put("error", "");

		if (isInitialized()) {
			try {
				props.put("phase", StatusContainer.getPhase().toString());
			} catch (Throwable e) {
				props.put("error", e.getMessage());
			}
			try {
				props.put("online", isOnline());
			} catch (Throwable e) {
				props.put("error", e.getMessage());
			}
			try {
				props.put("weight", ServiceProviderFactory.getServerWeight());
			} catch (Throwable e) {
				props.put("error", e.getMessage());
			}
			try {
				Map<String, ProviderConfig<?>> serviceProviders = ServiceFactory.getAllServiceProviders();
				props.put("services.count", serviceProviders.size());
			} catch (Throwable e) {
				props.put("error", e.getMessage());
			}

			String error = providerStatusChecker.checkError();
			if (!StringUtils.isBlank(error)) {
				props.put("error", error);
			}
		} else if (RegistryManager.getInitializeException() != null) {
			Throwable t = RegistryManager.getInitializeException();
			StringBuilder error = new StringBuilder(t.getMessage());
			if (t.getCause() != null) {
				error.append(", caused by: ").append(t.getCause().getMessage());
			}
			props.put("error", error.toString());
		}

		return props;
	}

}
