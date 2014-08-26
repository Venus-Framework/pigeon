package com.dianping.pigeon.governor.listener;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.Constants.Action;
import com.dianping.pigeon.governor.util.Constants.Environment;

public class OfflineListenerBean {

	private static Logger logger = Logger.getLogger(OfflineListenerBean.class);

	private ConfigCache configManager;

	private String action;

	private Map<Environment, Action> actionMap;
	
	private Map<Environment, CuratorRegistry> registryMap;

	public OfflineListenerBean() {
	}

	private void initRegistry(CuratorRegistry registry) throws Exception {
		boolean exists = registry.exists("/DP");
		if (!exists) {
			registry.create("/DP");
		}
		exists = registry.exists("/DP/SERVICE");
		if (!exists) {
			registry.create("/DP/SERVICE");
		}
	}

	private void initConfig() {
		try {
			configManager = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress());
			configManager.addChange(new ConfigChangeHandler());
			action = configManager.getProperty("pigeon.governor.offlinelistener.action");
		} catch (LionException e) {
			logger.error("", e);
		}
		if(StringUtils.isBlank(action)) {
			action = "dev:log,alpha:log,qa:log";
		}
		actionMap = new LinkedHashMap<Environment, Action>();
		registryMap = new LinkedHashMap<Environment, CuratorRegistry>();
		parseAction();
	}

	private void parseAction() {
		if (StringUtils.isBlank(action)) {
			logger.error("action is null");
			return;
		}

		String[] envActionList = action.split(",");
		for (String envAction : envActionList) {
			String[] envActionPair = envAction.split(":");
			Environment env = Environment.valueOf(envActionPair[0].trim());
			Action act = Action.valueOf(envActionPair[1].trim());
			actionMap.put(env, act);
			if(registryMap != null && registryMap.containsKey(env)) {
				registryMap.get(env).setAction(act);
			}
		}
	}

	public void init() throws Exception {
		initConfig();
		for (Environment env : actionMap.keySet()) {
			Action action = actionMap.get(env);
			CuratorRegistry registry = new CuratorRegistry();
			registryMap.put(env, registry);
			registry.init(env.name(), env.getZkAddress(), action);
			initRegistry(registry);
			List<String> services = registry.getChildren("/DP/SERVICE");
			registry.getServices().addAll(services);
			if (services != null) {
				for (String service : services) {
					String servicePath = "/DP/SERVICE/" + service;
					List<String> nodes = registry.getChildren(servicePath);
					registry.getEphemeralAddresses().put(servicePath, nodes);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		OfflineListenerBean bean = new OfflineListenerBean();
		bean.init();
		System.in.read();
	}

	class ConfigChangeHandler implements ConfigChange {

		@Override
		public void onChange(String key, String value) {
			if (Constants.KEY_ACTION.equals(key)) {
				action = value;
				parseAction();
			}
		}

	}
}
