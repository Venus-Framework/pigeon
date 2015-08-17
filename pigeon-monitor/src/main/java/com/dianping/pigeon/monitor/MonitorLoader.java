package com.dianping.pigeon.monitor;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.simple.SimpleMonitor;

public class MonitorLoader {

	private static Monitor monitor = ExtensionLoader.getExtension(Monitor.class);
	private static final Logger logger = LoggerLoader.getLogger(MonitorLoader.class);

	static {
		if (monitor == null) {
			monitor = new SimpleMonitor();
		}
		logger.info("monitor:" + monitor);
		monitor.init();
	}

	public static Monitor getMonitor() {
		return monitor;
	}
}
