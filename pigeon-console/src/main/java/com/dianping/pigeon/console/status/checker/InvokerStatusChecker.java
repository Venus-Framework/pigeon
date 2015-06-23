package com.dianping.pigeon.console.status.checker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.Server;

public class InvokerStatusChecker implements StatusChecker {

	private static final Logger logger = LoggerLoader.getLogger(InvokerStatusChecker.class);

	@Override
	public List<Map<String, Object>> collectStatusInfo() {
		List<Map<String, Object>> invokers = new ArrayList<Map<String, Object>>();
		if (GlobalStatusChecker.isInitialized()) {
			try {
				Collection<Server> servers = ProviderBootStrap.getServersMap().values();
				for (Server server : servers) {
					List<String> serverInvokers = server.getInvokerMetaInfo();
					if (serverInvokers != null) {
						for (String invoker : serverInvokers) {
							Map<String, Object> item = new HashMap<String, Object>();
							item.put("server", server.toString());
							item.put("from", invoker);
							invokers.add(item);
						}
					}
				}
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
		return invokers;
	}

	@Override
	public String checkError() {
		return null;
	}

}
