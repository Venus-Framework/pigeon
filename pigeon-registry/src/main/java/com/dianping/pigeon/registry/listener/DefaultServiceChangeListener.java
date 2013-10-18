package com.dianping.pigeon.registry.listener;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.HostInfo;
import com.dianping.pigeon.registry.cache.RegistryCache;
import com.dianping.pigeon.registry.cache.ServiceCache;
import com.dianping.pigeon.registry.cache.WeightCache;

public class DefaultServiceChangeListener implements ServiceChangeListener {

	private static final Logger logger = Logger.getLogger(DefaultServiceChangeListener.class);

	private final static int WEIGHT_DEFAULT = 1;

	public DefaultServiceChangeListener() {

	}

	@Override
	public synchronized void onServiceHostChange(String serviceName, List<String[]> hostList) {
		try {
			Set<HostInfo> newHpSet = parseHostPortList(serviceName, hostList);
			Set<HostInfo> oldHpSet = ServiceCache.serviceNameToHostInfos.get(serviceName);
			Set<HostInfo> toAddHpSet = Collections.emptySet();
			Set<HostInfo> toRemoveHpSet = Collections.emptySet();
			if (oldHpSet == null) {
				toAddHpSet = newHpSet;
			} else {
				toRemoveHpSet = new HashSet<HostInfo>(oldHpSet);
				toRemoveHpSet.removeAll(newHpSet);
				toAddHpSet = new HashSet<HostInfo>(newHpSet);
				toAddHpSet.removeAll(oldHpSet);
			}
			for (HostInfo hostPort : toAddHpSet) {
				RegistryEventListener.providerAdded(serviceName, hostPort.getHost(), hostPort.getPort(),
						hostPort.getWeight());
			}
			for (HostInfo hostPort : toRemoveHpSet) {
				RegistryEventListener.providerRemoved(serviceName, hostPort.getHost(), hostPort.getPort());
			}
		} catch (Exception e) {
			logger.error("error change service host", e);
		}
	}

	private Set<HostInfo> parseHostPortList(String serviceName, List<String[]> hostList) {
		Set<HostInfo> hpSet = new HashSet<HostInfo>();
		if (hostList != null) {
			for (String[] parts : hostList) {
				String host = parts[0];
				String port = parts[1];
				String serviceAddress = host + ":" + port;
				Integer weight = WeightCache.getInstance().getWeight(serviceName, serviceAddress);
				if (weight == null) {
					try {
						weight = RegistryCache.getInstance().getServiceWeigth(serviceAddress);
					} catch (Exception e) {
						logger.error("error get weight from PigeonClient for host " + host + ", use default weight", e);
						weight = WEIGHT_DEFAULT;
					}
				}
				hpSet.add(new HostInfo(host, Integer.parseInt(port), weight));
			}
		}
		return hpSet;
	}

	@Override
	public synchronized void onHostWeightChange(String connect, int weight) {

		int colonIdx = connect.indexOf(":");
		String host = connect.substring(0, colonIdx);
		int port = Integer.parseInt(connect.substring(colonIdx + 1));
		RegistryEventListener.hostWeightChanged(host, port, weight);
	}

}
