package com.dianping.pigeon.governor.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.pigeon.governor.listener.CuratorRegistry;

public class OfflineListenerBean {

	private static Logger logger = Logger.getLogger(OfflineListenerBean.class);

	public OfflineListenerBean() {
	}

	public void init() throws Exception {
		CuratorRegistry registry = new CuratorRegistry();
		registry.init("dev.lion.dp:2181");
		List<String> services = registry.getChildren("/DP/SERVICE");
		registry.getServices().addAll(services);
		if (services != null) {
			for (String service : services) {
				System.out.println(service);
				String servicePath = "/DP/SERVICE/" + service;
				List<String> nodes = registry.getChildren(servicePath);
				registry.getEphemeralAddresses().put(servicePath, nodes);
				System.out.println(nodes);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		OfflineListenerBean bean = new OfflineListenerBean();
		bean.init();
		System.in.read();
	}
}
