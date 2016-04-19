/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.SecurityUtils;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

/**
 * @author xiangwu
 * 
 */
public class SecurityFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(SecurityFilter.class);
	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static final String KEY_APP_SECRETS = "pigeon.invoker.token.app.secrets";
	private static final String KEY_TOKEN_ENABLE = "pigeon.invoker.token.enable";
	private static volatile ConcurrentHashMap<String, String> appSecrets = new ConcurrentHashMap<String, String>();

	public SecurityFilter() {
		configManager.getBooleanValue(KEY_TOKEN_ENABLE, true);
		parseAppSecrets(configManager.getStringValue(KEY_APP_SECRETS, ""));
		ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
	}

	private static void parseAppSecrets(String config) {
		if (StringUtils.isNotBlank(config)) {
			ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();
			try {
				String[] pairArray = config.split(",");
				for (String str : pairArray) {
					if (StringUtils.isNotBlank(str)) {
						String[] pair = str.split(":");
						if (pair != null && pair.length == 2) {
							map.put(pair[0].trim(), pair[1].trim());
						}
					}
				}
				appSecrets.clear();
				appSecrets = map;
			} catch (RuntimeException e) {
				logger.error("error while parsing app secret configuration:" + config, e);
			}
		} else {
			appSecrets.clear();
		}
	}

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith(KEY_APP_SECRETS)) {
				parseAppSecrets(value);
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {

		}

		@Override
		public void onKeyRemoved(String key) {

		}

	}

	private static int getCurrentTime() {
		return (int) (System.currentTimeMillis() / 1000);
	}

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		InvocationRequest request = invocationContext.getRequest();
		if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
			InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
			String secret = invokerConfig.getSecret();
			if (StringUtils.isBlank(secret) && configManager.getBooleanValue(KEY_TOKEN_ENABLE, true)) {
				String targetApp = RegistryManager.getInstance().getReferencedAppFromCache(
						invocationContext.getClient().getAddress());
				secret = appSecrets.get(targetApp);
			}
			if (StringUtils.isNotBlank(secret)) {
				int timestamp = getCurrentTime();
				request.getRequestValues().put(Constants.REQUEST_KEY_TIMESTAMP, timestamp);
				request.getRequestValues().put(Constants.REQUEST_KEY_VERSION, 0);
				String data = request.getServiceName() + "#" + request.getMethodName() + "#" + timestamp;
				request.getRequestValues().put(Constants.REQUEST_KEY_TOKEN, SecurityUtils.encrypt(data, secret));
			}
		}
		return handler.handle(invocationContext);
	}
}
