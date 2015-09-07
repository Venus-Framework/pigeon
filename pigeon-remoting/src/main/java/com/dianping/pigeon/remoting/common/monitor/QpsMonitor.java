package com.dianping.pigeon.remoting.common.monitor;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;

public class QpsMonitor {

	private static Logger logger = LoggerLoader.getLogger(QpsMonitor.class);

	private static final String rangeConfig = ConfigManagerLoader
			.getConfigManager()
			.getStringValue(
					"pigeon.monitor.qps.range",
					"10,50,100,300,500,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,12000,14000,16000,18000,20000,25000,30000,35000,40000,45000,50000,55000,60000,65000,70000,75000,80000,85000,90000,100000,110000,120000,130000,140000,150000,160000,170000,180000,190000,200000");

	private static int[] rangeArray;

	private static final boolean enableMonitor = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.monitor.qps.enable", true);

	public static final int logMin = ConfigManagerLoader.getConfigManager().getIntValue("pigeon.monitor.qps.log.min",
			1000);

	private static Monitor monitor = MonitorLoader.getMonitor();

	private static class QpsMonitorHolder {
		public static final QpsMonitor INSTANCE = new QpsMonitor();
	}

	public static QpsMonitor getInstance() {
		return QpsMonitorHolder.INSTANCE;
	}

	private QpsMonitor() {
		if (enableMonitor) {
			init();
		}
	}

	private void init() {
		rangeArray = initRangeArray(rangeConfig);
	}

	private int[] initRangeArray(String rangeConfig) {
		String[] range = rangeConfig.split(",");
		int end = Integer.valueOf(range[range.length - 1]);
		int[] rangeArray = new int[end];
		int rangeIndex = 0;
		for (int i = 0; i < end; i++) {
			if (range.length > rangeIndex) {
				int value = Integer.valueOf(range[rangeIndex]);
				if (i >= value) {
					rangeIndex++;
				}
				rangeArray[i] = value;
			}
		}
		return rangeArray;
	}

	public void logQps(String eventName, int qps, String time) {
		if (enableMonitor) {
			try {
				log(qps, rangeArray, eventName, time);
			} catch (Throwable t) {
				logger.warn("error while logging qps:" + t.getMessage());
			}
		}
		if (qps > logMin) {
			logger.info(eventName + "=" + qps + ", time=" + time);
		}
	}

	private void log(int qps, int[] rangeArray, String eventName, String time) {
		if (rangeArray != null && rangeArray.length > 0) {
			String value = ">" + rangeArray[rangeArray.length - 1];
			if (rangeArray.length > qps) {
				value = "<" + rangeArray[qps];
			}
			monitor.logEvent(eventName, value, "qps=" + qps + "&time=" + time);
		}
	}
}
