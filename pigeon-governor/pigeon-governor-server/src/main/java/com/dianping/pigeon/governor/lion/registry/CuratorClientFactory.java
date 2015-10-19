package com.dianping.pigeon.governor.lion.registry;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;

public class CuratorClientFactory {
	
	private static Logger logger = LoggerLoader.getLogger(CuratorClientFactory.class);

	private static volatile boolean isInitialized = false;
	
	public static DefaultCuratorClient lionZkClient;

	public static DefaultCuratorClient pigeonZkClient;
	
	private CuratorClientFactory(){};
	
	public static void init(){
		if (!isInitialized) {
			try {
				lionZkClient = new DefaultCuratorClient("alpha.lion.dp:2182");
				pigeonZkClient = new DefaultCuratorClient("alpha.core.dp:2181");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Failed to create curatorClient");
				return ;
			}
			
			isInitialized = true;
		}
	}
	
}
