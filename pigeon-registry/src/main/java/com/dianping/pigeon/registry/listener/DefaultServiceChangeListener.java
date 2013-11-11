package com.dianping.pigeon.registry.listener;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.HostInfo;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.util.Constants;

public class DefaultServiceChangeListener implements ServiceChangeListener {

	private static final Logger logger = LoggerLoader.getLogger(DefaultServiceChangeListener.class);

	private final static int WEIGHT_DEFAULT = 1;

	public DefaultServiceChangeListener() {

	}

	@Override
	public synchronized void onServiceHostChange(String serviceName, List<String[]> hostList) {
		try {
			Set<HostInfo> newHpSet = parseHostPortList(serviceName, hostList);
			Set<HostInfo> oldHpSet = RegistryManager.getInstance().getServiceServers(serviceName);
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
			if(logger.isInfoEnabled()) {
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

				int weight = Constants.DEFAULT_WEIGHT_INT;
				try {
					weight = RegistryManager.getInstance().getServiceWeight(serviceAddress);
				} catch (Exception e) {
					logger.error("error get weight from PigeonClient for host " + host + ", use default weight", e);
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
