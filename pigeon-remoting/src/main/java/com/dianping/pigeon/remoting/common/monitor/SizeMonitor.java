package com.dianping.pigeon.remoting.common.monitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;

public class SizeMonitor {

	private static Logger logger = LogManager.getLogger(SizeMonitor.class);

	private static Monitor monitor = MonitorLoader.getMonitor();

	private static final String sizeRangeConfig = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.monitor.size.range", "1,2,4,8,16,32,64,128,256,512,1024");

	private static int[] sizeRangeArray;

	private static final long sizeMin = ConfigManagerLoader.getConfigManager().getLongValue(
			"pigeon.monitor.msgsize.min", 0);

	private static class SizeHolder {
		public static final SizeMonitor INSTANCE = new SizeMonitor();
	}

	public static SizeMonitor getInstance() {
		return SizeHolder.INSTANCE;
	}

	private SizeMonitor() {
		init();
	}

	private void init() {
		sizeRangeArray = initRangeArray(sizeRangeConfig);
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

	public void logSize(int size, String event, String source) {
		if (size > sizeMin) {
			try {
				logSize(size, sizeRangeArray, event, source);
			} catch (Throwable t) {
				logger.warn("error while logging size:" + t.getMessage());
			}
		}
	}

	private void logSize(int size, int[] rangeArray, String eventName, String source) {
		if (size > 0 && rangeArray != null && rangeArray.length > 0) {
			String value = ">" + rangeArray[rangeArray.length - 1] + "k";
			int sizeK = (int) Math.ceil(size * 1d / 1024);
			if (rangeArray.length > sizeK) {
				value = "<" + rangeArray[sizeK] + "k";
			}
			monitor.logEvent(eventName, value, size + "");
		}
	}
}
