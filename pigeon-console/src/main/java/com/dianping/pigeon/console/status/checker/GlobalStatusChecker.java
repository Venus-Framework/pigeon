package com.dianping.pigeon.console.status.checker;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.service.PublishStatus;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;

public class GlobalStatusChecker {

	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static final StatusChecker providerStatusChecker = new ProviderStatusChecker();

	public static Map<String, Object> getGlobalStatusProperties() {
		Map<String, Object> props = new LinkedHashMap<String, Object>();
		props.put("env", configManager.getEnv());
		props.put("swimlane", configManager.getGroup());
		props.put("weight", ServiceProviderFactory.getServerWeight());

		String error = providerStatusChecker.checkError();
		if (!StringUtils.isBlank(error)) {
			props.put("error", error);
			props.put("status", "error");
		}
		Map<String, ProviderConfig<?>> providers = ServiceFactory.getAllServiceProviders();
		if (providers.isEmpty()) {// client-side
			// set published
			props.put("published", "none");
			// set status
			if (!"error".equals(props.get("status"))) {
				props.put("status", "ok");
			}
		} else {// server-side
			// set published
			PublishStatus status = ServiceProviderFactory.getPublishStatus();
			if (status.equals(PublishStatus.PUBLISHED) || status.equals(PublishStatus.WARMINGUP)
					|| status.equals(PublishStatus.WARMEDUP)) {
				props.put("published", "true");
			} else {
				props.put("published", "false");
			}
			// set status
			if (!"error".equals(props.get("status"))) {
				if ("true".equals(props.get("published"))) {
					props.put("status", "ok");
				} else {
					props.put("status", status.toString());
				}
			}
		}

		return props;
	}

}
