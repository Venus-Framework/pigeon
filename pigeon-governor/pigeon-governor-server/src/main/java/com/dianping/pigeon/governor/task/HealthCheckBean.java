package com.dianping.pigeon.governor.task;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.util.IPUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HealthCheckBean {

	private Logger logger = LogManager.getLogger(HealthCheckBean.class);
	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	public HealthCheckBean() {
	}

	public void init() {
		boolean enable = true;
		String ip = configManager.getStringValue("pigeon-governor-server.enable.ip", "");

		if (!IPUtils.getFirstNoLoopbackIP4Address().equals(ip)) {
			enable = false;
		}

		if (enable) {
			new HealthCheckManager().start();
			logger.info("HealthCheckManager started...");
		} else {
			logger.info("HealthCheckManager not start...");
		}
	}

	public static void main(String[] args) throws Exception {
		HealthCheckBean bean = new HealthCheckBean();
		bean.init();
		System.in.read();
	}
}
