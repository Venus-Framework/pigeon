package com.dianping.pigeon.governor.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.governor.lion.registry.ZkListenerFactory;

public class MyServletContextListener implements ServletContextListener {

	private Logger logger = LogManager.getLogger();
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("----------- 终止环境 ------------");
		ZkListenerFactory.destroy();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("----------- 启动环境 ------------");
		ZkListenerFactory.init();
	}

}
