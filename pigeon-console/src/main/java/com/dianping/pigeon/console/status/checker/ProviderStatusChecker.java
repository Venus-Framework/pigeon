package com.dianping.pigeon.console.status.checker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.route.statistics.CapacityBucket;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;
import com.dianping.pigeon.util.CollectionUtils;

public class ProviderStatusChecker implements StatusChecker {

	private static final Logger logger = LoggerLoader.getLogger(ProviderStatusChecker.class);

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static final boolean CHECK_PROVIDER_EXIST = configManager.getBooleanValue(
			"pigeon.status.checkproviderexist", false);

	private static final boolean CHECK_PROVIDER_AVAILABLE = configManager.getBooleanValue(
			"pigeon.status.checkprovideravailable", false);

	@Override
	public List<Map<String, Object>> collectStatusInfo() {
		List<Map<String, Object>> providers = new ArrayList<Map<String, Object>>();
		if (GlobalStatusChecker.isInitialized()) {
			try {
				Map<String, CapacityBucket> buckets = ServiceStatisticsHolder.getCapacityBuckets();
				Map<String, List<Client>> heartbeats = ClientManager.getInstance().getHeartTask().getWorkingClients();
				if (heartbeats != null) {
					for (String key : heartbeats.keySet()) {
						List<Client> clients = heartbeats.get(key);
						for (Client client : clients) {
							Map<String, Object> item = new LinkedHashMap<String, Object>();
							item.put("service", key);
							item.put("to", client.getAddress());
							item.put("weight", RegistryManager.getInstance().getServiceWeight(client.getAddress()));
							int requests = 0;
							if (client.getAddress() != null && buckets.get(client.getAddress()) != null) {
								requests = buckets.get(client.getAddress()).getLastSecondRequest();
							}
							item.put("requestsInLastSecond", requests);
							providers.add(item);
						}
					}
				}
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
		return providers;
	}

	public String checkError() {
		if (GlobalStatusChecker.isInitialized() && (CHECK_PROVIDER_EXIST || CHECK_PROVIDER_AVAILABLE)) {
			try {
				Map<InvokerConfig<?>, Object> serviceInvokers = ServiceFactory.getAllServiceInvokers();
				if (!serviceInvokers.isEmpty()) {
					for (InvokerConfig<?> invokerConfig : serviceInvokers.keySet()) {
						if (CHECK_PROVIDER_EXIST) {
							ClientManager.getInstance().getServiceAddress(invokerConfig.getUrl(),
									invokerConfig.getGroup(), invokerConfig.getVip());
						}
						if (CHECK_PROVIDER_AVAILABLE) {
							Map<String, List<Client>> clientsMap = ClientManager.getInstance().getHeartTask()
									.getWorkingClients();
							List<Client> clients = clientsMap.get(invokerConfig.getUrl());
							if (CollectionUtils.isEmpty(clients)) {
								String error = "no available provider found for service:" + invokerConfig.getUrl();
								logger.error(error);
								return error;
							}
						}
					}
				}
			} catch (Throwable t) {
				String error = "error while checking service provider";
				logger.error(error, t);
				return error + ", caused by " + t.toString();
			}
		}
		return null;
	}
}
