/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;

public class ProviderAvailableListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(ProviderAvailableListener.class);

	private Map<String, List<Client>> workingClients;

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private final long interval = configManager.getLongValue("pigeon.providerlistener.interval", 3000);

	private boolean isAvailableClients(List<Client> clientList) {
		boolean available = true;
		if (CollectionUtils.isEmpty(clientList)) {
			available = false;
		} else {
			int weight = 0;
			for (Client client : clientList) {
				int w = RegistryManager.getInstance().getServiceWeight(client.getAddress());
				if (w > 0 && client.isConnected() && client.isActive()) {
					weight += w;
				}
			}
			if (weight == 0) {
				available = false;
			}
		}
		return available;
	}

	public void run() {
		long sleepTime = interval;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(sleepTime);
				long now = System.currentTimeMillis();
				if (!CollectionUtils.isEmpty(this.getWorkingClients())) {
					for (String serviceName : this.getWorkingClients().keySet()) {
						boolean isAvailable = isAvailableClients(this.getWorkingClients().get(serviceName));
						if (!isAvailable) {
							logger.info("check provider available, no available provider for service:" + serviceName);
							ClientManager.getInstance().registerServiceInvokers(serviceName, configManager.getGroup(),
									null);
							if (StringUtils.isNotBlank(configManager.getGroup())) {
								isAvailable = isAvailableClients(this.getWorkingClients().get(serviceName));
								if (!isAvailable) {
									logger.info("check provider available with default group, no available provider for service:"
											+ serviceName);
									ClientManager.getInstance().registerServiceInvokers(serviceName,
											Constants.DEFAULT_GROUP, null);
								}
							}
						}
					}
				}
				Map<String, Set<HostInfo>> serviceHosts = RegistryManager.getInstance().getAllServiceServers();
				for (String serviceName : serviceHosts.keySet()) {
					if (!this.getWorkingClients().containsKey(serviceName)) {
						logger.info("check provider available, no available provider for service:" + serviceName);
						ClientManager.getInstance()
								.registerServiceInvokers(serviceName, configManager.getGroup(), null);
						if (StringUtils.isNotBlank(configManager.getGroup())) {
							boolean isAvailable = isAvailableClients(this.getWorkingClients().get(serviceName));
							if (!isAvailable) {
								logger.info("check provider available with default group, no available provider for service:"
										+ serviceName);
								ClientManager.getInstance().registerServiceInvokers(serviceName,
										Constants.DEFAULT_GROUP, null);
							}
						}
					}
				}

				sleepTime = interval - (System.currentTimeMillis() - now);
			} catch (Throwable e) {
				logger.error("[provideravailable] task failed:" + e.getCause());
			} finally {
				if (sleepTime < 1000) {
					sleepTime = 1000;
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
