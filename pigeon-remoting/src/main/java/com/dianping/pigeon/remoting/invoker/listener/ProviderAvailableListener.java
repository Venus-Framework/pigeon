/**
WS  * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

public class ProviderAvailableListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(ProviderAvailableListener.class);

	private Map<String, List<Client>> workingClients;

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static long interval = configManager.getLongValue("pigeon.providerlistener.interval", 3000);

	private static int providerAvailableLeast = configManager.getIntValue("pigeon.providerlistener.availableleast", 1);

	private static String ignoredServices = configManager.getStringValue("pigeon.providerlistener.ignoredservices", "");

	public ProviderAvailableListener() {
		configManager.registerConfigChangeListener(new InnerConfigChangeListener());
	}

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith("pigeon.providerlistener.availableleast")) {
				try {
					providerAvailableLeast = Integer.valueOf(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith("pigeon.providerlistener.interval")) {
				try {
					interval = Long.valueOf(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith("pigeon.providerlistener.ignoredservices")) {
				try {
					ignoredServices = value;
				} catch (RuntimeException e) {
				}
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {
		}

		@Override
		public void onKeyRemoved(String key) {
		}

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
		long sleepTime = interval;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(sleepTime);
				Set<InvokerConfig<?>> services = ServiceFactory.getAllServiceInvokers().keySet();
				Map<String, String> serviceGroupMap = new HashMap<String, String>();
				for (InvokerConfig<?> invokerConfig : services) {
					serviceGroupMap
							.put(invokerConfig.getUrl(), invokerConfig.getGroup() + "#" + invokerConfig.getVip());
				}
				long now = System.currentTimeMillis();
				for (String url : serviceGroupMap.keySet()) {
					if (StringUtils.isNotBlank(ignoredServices) && ignoredServices.indexOf(url) != -1) {
						continue;
					}
					String groupValue = serviceGroupMap.get(url);
					String group = groupValue.substring(0, groupValue.lastIndexOf("#"));
					String vip = groupValue.substring(groupValue.lastIndexOf("#") + 1);
					if(vip != null && vip.startsWith("console:")) {
						continue;
					}

					int available = getAvailableClients(this.getWorkingClients().get(url));
					if (available < providerAvailableLeast) {
						logger.warn("check provider available for service:" + url);
						String error = null;
						try {
							ClientManager.getInstance().registerServiceInvokers(url, group, vip);
						} catch (Throwable e) {
							error = e.getMessage();
						}
						if (StringUtils.isNotBlank(group)) {
							available = getAvailableClients(this.getWorkingClients().get(url));
							if (available < providerAvailableLeast) {
								logger.warn("check provider available with default group for service:" + url);
								try {
									ClientManager.getInstance().registerServiceInvokers(url, Constants.DEFAULT_GROUP,
											vip);
								} catch (Throwable e) {
									error = e.getMessage();
								}
							}
						}
						if (error != null) {
							logger.warn("[provideravailable] failed to get providers, caused by " + error);
						}
					}
				}
				sleepTime = interval - (System.currentTimeMillis() - now);
			} catch (Throwable e) {
				logger.warn("[provideravailable] task failed:", e);
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
