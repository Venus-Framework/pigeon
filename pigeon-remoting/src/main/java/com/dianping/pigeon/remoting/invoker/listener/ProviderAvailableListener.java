/**
WS  * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProviderAvailableListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(ProviderAvailableListener.class);

	private Map<String, List<Client>> workingClients;

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static final String KEY_INTERVAL = "pigeon.providerlistener.interval";
	
	private static final String KEY_AVAILABLE_LEAST = "pigeon.providerlistener.availableleast";

	public ProviderAvailableListener() {
		configManager.getLongValue(KEY_INTERVAL, 3000);
		configManager.getIntValue(KEY_AVAILABLE_LEAST, 1);
	}

	private int getAvailableClients(List<Client> clientList) {
		int available = 0;
		if (CollectionUtils.isEmpty(clientList)) {
			available = 0;
		} else {
			for (Client client : clientList) {
				int w = RegistryManager.getInstance().getServiceWeight(client.getAddress());
				if (w > 0 && client.isConnected() && client.isActive()) {
					available += w;
				}
			}
		}
		return available;
	}

	public void run() {
		long sleepTime = configManager.getLongValue(KEY_INTERVAL, 3000);
		int checkCount = 0;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(sleepTime);
				try {
					checkReferencedServices();
				} catch (Throwable e) {
					logger.info("check referenced services failed:", e);
				}

				Set<InvokerConfig<?>> services = ServiceFactory.getAllServiceInvokers().keySet();
				Map<String, String> serviceGroupMap = new HashMap<String, String>();
				for (InvokerConfig<?> invokerConfig : services) {
					String vip = "";
					if (StringUtils.isNotBlank(invokerConfig.getVip())) {
						vip = invokerConfig.getVip();
					}
					serviceGroupMap.put(invokerConfig.getUrl(), invokerConfig.getGroup() + "#" + vip);
				}
				long now = System.currentTimeMillis();
				for (String url : serviceGroupMap.keySet()) {
					String groupValue = serviceGroupMap.get(url);
					String group = groupValue.substring(0, groupValue.lastIndexOf("#"));
					String vip = groupValue.substring(groupValue.lastIndexOf("#") + 1);
					if (vip != null && vip.startsWith("console:")) {
						continue;
					}

					int available = getAvailableClients(this.getWorkingClients().get(url));
					if (available < configManager.getIntValue(KEY_AVAILABLE_LEAST, 1)) {
						logger.info("check provider available for service:" + url);
						String error = null;
						try {
							ClientManager.getInstance().registerClients(url, group, vip);
						} catch (Throwable e) {
							error = e.getMessage();
						}
						// if (StringUtils.isNotBlank(group)) {
						// available =
						// getAvailableClients(this.getWorkingClients().get(url));
						// if (available < providerAvailableLeast) {
						// logger.info("check provider available with default group for service:"
						// + url);
						// try {
						// ClientManager.getInstance().registerClients(url,
						// Constants.DEFAULT_GROUP, vip);
						// } catch (Throwable e) {
						// error = e.getMessage();
						// }
						// }
						// }
						if (error != null) {
							logger.warn("[provider-available] failed to get providers, caused by:" + error);
						}
					}
				}
				sleepTime = configManager.getLongValue(KEY_INTERVAL, 3000) - (System.currentTimeMillis() - now);

				// close register thread pool
				/*
				 * if (++checkCount > 0) {
				 * ClientManager.getInstance().closeRegisterThreadPool(); }
				 */
			} catch (Throwable e) {
				logger.info("[provider-available] task failed:", e);
			} finally {
				if (sleepTime < 1000) {
					sleepTime = 1000;
				}
			}
		}
	}

	private void checkReferencedServices() {
		Registry registry = RegistryManager.getInstance().getRegistry();
		Map<String, Set<HostInfo>> serviceAddresses = RegistryManager.getInstance().getAllReferencedServiceAddresses();
		for (String key : serviceAddresses.keySet()) {
			Set<HostInfo> hosts = serviceAddresses.get(key);
			if (hosts != null) {
				for (HostInfo host : hosts) {
					if (host.getApp() == null) {
						String app = registry.getServerApp(host.getConnect());
						logger.info("set " + host.getConnect() + "'s app to " + app);
						host.setApp(app);
						RegistryManager.getInstance().setReferencedApp(host.getConnect(), app);
					}
					if (host.getVersion() == null) {
						String version = registry.getServerVersion(host.getConnect());
						logger.info("set " + host.getConnect() + "'s version to " + version);
						host.setVersion(version);
						RegistryManager.getInstance().setReferencedVersion(host.getConnect(), version);
					}
				}
			}
		}
	}

	public Map<String, List<Client>> getWorkingClients() {
		return workingClients;
	}

	public void setWorkingClients(Map<String, List<Client>> workingClients) {
		this.workingClients = workingClients;
	}
}
