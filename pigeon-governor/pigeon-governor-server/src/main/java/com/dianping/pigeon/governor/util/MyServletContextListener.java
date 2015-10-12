package com.dianping.pigeon.governor.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.dianping.pigeon.governor.lion.registry.LionCuratorClient;
import com.dianping.pigeon.governor.lion.registry.LionCuratorRegistry;
import com.dianping.pigeon.governor.lion.registry.LionRegistryManager;
import com.dianping.pigeon.registry.Registry;

public class MyServletContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("----------- 终止环境 ------------");
		
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("----------- 启动环境 ------------");
		
		dynamicLoad();
	}

	public static void dynamicLoad() {
		Registry registry = LionRegistryManager.getInstance().getRegistry();
		LionCuratorClient client = ((LionCuratorRegistry) registry).getLionCuratorClient();
		try {
			client.watchChildren("/DP/SERVER");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
