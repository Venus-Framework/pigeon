package com.dianping.pigeon.governor.task;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.dianping.pigeon.log.LoggerLoader;

public class HealthCheckBean {

	private static Logger logger = LogManager.getLogger(HealthCheckBean.class);

	public HealthCheckBean() {
	}

	public void init() {
//		ConfigurationSource source;
//		try {
//			source = new ConfigurationSource(HealthCheckManager.class.getResourceAsStream("/log4j2.xml"));
//			Configurator.shutdown(LoggerLoader.getLoggerContext());
//			Configurator.initialize(null, source, LoggerLoader.getLoggerContext());
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
		new HealthCheckManager().start();
		logger.info("HealthCheckManager started");
	}

	public static void main(String[] args) throws Exception {
		HealthCheckBean bean = new HealthCheckBean();
		bean.init();
		System.in.read();
	}
}
