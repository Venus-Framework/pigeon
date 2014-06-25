package com.dianping.pigeon.remoting.invoker.process;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.process.ServiceStatusChecker;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.util.CollectionUtils;

public class ServiceInvokerStatusChecker implements ServiceStatusChecker {

	private static final Logger logger = LoggerLoader.getLogger(ServiceInvokerStatusChecker.class);

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static final boolean CHECK_PROVIDER_EXIST = configManager.getBooleanValue(
			"pigeon.status.checkproviderexist", false);

	private static final boolean CHECK_PROVIDER_AVAILABLE = configManager.getBooleanValue(
			"pigeon.status.checkprovideravailable", false);

	@Override
	public String check() {
		Map<InvokerConfig<?>, Object> serviceInvokers = ServiceFactory.getAllServiceInvokers();
		if (!serviceInvokers.isEmpty()) {
			for (InvokerConfig<?> invokerConfig : serviceInvokers.keySet()) {
				try {
					if (CHECK_PROVIDER_EXIST) {
						ClientManager.getInstance().getServiceAddress(invokerConfig.getUrl(), invokerConfig.getGroup(),
								invokerConfig.getVip());
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
				} catch (Throwable t) {
					String error = "error while retrieving address for service:" + invokerConfig.getUrl();
					logger.error(error, t);
					return error + ", caused by " + t.toString();
				}
			}
		}
		return null;
	}

}
