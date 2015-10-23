package com.dianping.pigeon.governor.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.dianping.pigeon.governor.lion.registry.HostDbProcess;
import com.dianping.pigeon.governor.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.governor.lion.registry.ZkListenerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class MyServletContextListener implements ServletContextListener {

	private Logger logger = LogManager.getLogger();

	private ApplicationContext applicationContext;

	private HostDbProcess hostDbProcess;


	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("----------- 终止环境 ------------");
		//ZkListenerFactory.destroy();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("----------- 启动环境 ------------");
		//ZkListenerFactory.init();

		applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
		hostDbProcess = (HostDbProcess) BeanFactoryUtils.beanOfType(applicationContext, HostDbProcess.class);
		//hostDbProcess.init();
	}

}
