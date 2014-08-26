package com.dianping.pigeon.governor.listener;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.pigeon.governor.util.Constants.Action;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.util.CollectionUtils;

public class ServiceOfflineListener {

	private static Logger logger = LoggerLoader.getLogger(ServiceOfflineListener.class);

	private CuratorRegistry curatorRegistry;

	public ServiceOfflineListener(CuratorRegistry curatorRegistry) {
		this.curatorRegistry = curatorRegistry;
	}

	public void offline(String serviceName, String host, String group) {
		try {
			List<String> hostList = curatorRegistry.getEphemeralServiceAddress(serviceName, group);
			if (!CollectionUtils.isEmpty(hostList)) {
				Collections.sort(hostList);
				for (String h : hostList) {
					if (h.equals(host)) {
						return;
					}
				}
			}
			if (Action.remove.equals(curatorRegistry.getAction()) || Action.remove.equals(curatorRegistry.getAction())) {
				curatorRegistry.unregisterPersistentNode(serviceName, group, host);
			} else {
				logger.warn(curatorRegistry.getEnv() + "#node offline:" + host + "," + serviceName + "," + group);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
