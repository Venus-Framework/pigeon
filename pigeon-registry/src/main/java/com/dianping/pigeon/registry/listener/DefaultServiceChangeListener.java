package com.dianping.pigeon.registry.listener;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.util.Utils;

public class DefaultServiceChangeListener implements ServiceChangeListener {

	private static final Logger logger = LoggerLoader.getLogger(DefaultServiceChangeListener.class);

	public DefaultServiceChangeListener() {
	}

	@Override
	public void onServiceHostChange(String serviceName, List<String[]> hostList) {
		try {
			Set<HostInfo> newHpSet = parseHostPortList(serviceName, hostList);
			Set<HostInfo> oldHpSet = RegistryManager.getInstance().getReferencedServiceAddresses(serviceName);
			Set<HostInfo> toAddHpSet = Collections.emptySet();
			Set<HostInfo> toRemoveHpSet = Collections.emptySet();
			if (oldHpSet == null) {
				toAddHpSet = newHpSet;
			} else {
				toRemoveHpSet = Collections.newSetFromMap(new ConcurrentHashMap<HostInfo, Boolean>());
				toRemoveHpSet.addAll(oldHpSet);
				toRemoveHpSet.removeAll(newHpSet);
				toAddHpSet = Collections.newSetFromMap(new ConcurrentHashMap<HostInfo, Boolean>());
				toAddHpSet.addAll(newHpSet);
				toAddHpSet.removeAll(oldHpSet);
			}
			if (logger.isInfoEnabled()) {
				logger.info("service hosts changed, to added hosts:" + toAddHpSet);
				logger.info("service hosts changed, to removed hosts:" + toRemoveHpSet);
			}
			for (HostInfo hostPort : toAddHpSet) {
				RegistryEventListener.providerAdded(serviceName, hostPort.getHost(), hostPort.getPort(),
						hostPort.getWeight());
			}
			for (HostInfo hostPort : toRemoveHpSet) {
				RegistryEventListener.providerRemoved(serviceName, hostPort.getHost(), hostPort.getPort());
			}
		} catch (Throwable e) {
			logger.error("error with service host change", e);
		}
	}

	private Set<HostInfo> parseHostPortList(String serviceName, List<String[]> hostList) {
		Set<HostInfo> hpSet = Collections.newSetFromMap(new ConcurrentHashMap<HostInfo, Boolean>());
		if (hostList != null) {
			for (String[] parts : hostList) {
				String host = parts[0];
				String port = parts[1];
				String serviceAddress = host + ":" + port;

				int weight = RegistryManager.getInstance().getServiceWeight(serviceAddress);
				hpSet.add(new HostInfo(host, Integer.parseInt(port), weight));
			}
		}
		return hpSet;
	}

	@Override
	public void onHostWeightChange(String connect, int weight) {
		HostInfo hostInfo = Utils.parseHost(connect, weight);
		if (hostInfo != null) {
			RegistryEventListener.hostWeightChanged(hostInfo.getHost(), hostInfo.getPort(), weight);
		}
	}

}
