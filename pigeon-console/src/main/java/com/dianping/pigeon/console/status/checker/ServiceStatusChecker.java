package com.dianping.pigeon.console.status.checker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.util.CollectionUtils;

public class ServiceStatusChecker implements StatusChecker {

	private static final Logger logger = LoggerLoader.getLogger(ServiceStatusChecker.class);

	@Override
	public List<Map<String, Object>> collectStatusInfo() {
		List<Map<String, Object>> services = new ArrayList<Map<String, Object>>();
		if (GlobalStatusChecker.isInitialized()) {
			try {
				Map<String, ProviderConfig<?>> serviceProviders = ServiceFactory.getAllServiceProviders();
				if (!CollectionUtils.isEmpty(serviceProviders)) {
					for (Entry<String, ProviderConfig<?>> entry : serviceProviders.entrySet()) {
						String serviceName = entry.getKey();
						ProviderConfig<?> providerConfig = entry.getValue();
						Class<?> beanClass = providerConfig.getService().getClass();
						int idxCglib = beanClass.getName().indexOf("$$EnhancerByCGLIB");
						if (idxCglib != -1) {
							try {
								beanClass = Class.forName(beanClass.getName().substring(0, idxCglib));
							} catch (ClassNotFoundException e) {
								throw new IllegalStateException("Failed to export remote service class "
										+ beanClass.getName(), e);
							}
						}
						Map<String, Object> item = new LinkedHashMap<String, Object>();
						item.put("name", serviceName);
						item.put("type", beanClass.getName());
						item.put("published", providerConfig.isPublished());
						services.add(item);
					}
				}
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
		return services;
	}

	public String checkError() {
		return null;
	}
}
