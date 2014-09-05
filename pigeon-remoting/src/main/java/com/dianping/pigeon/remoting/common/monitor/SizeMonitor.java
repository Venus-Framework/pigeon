package com.dianping.pigeon.remoting.common.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;

public class SizeMonitor {

	private static Logger logger = LoggerFactory.getLogger(SizeMonitor.class);

	private static MonitorLogger monitor = ExtensionLoader.getExtension(Monitor.class).getLogger();

	private static final String sizeRangeConfig = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.monitor.size.range", "1,2,4,8,16,32,64,128,256,512,1024");

	private static int[] sizeRangeArray;

	private static final boolean enableMonitor = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.monitor.size.enable", true);

	private static class SizeHolder {
		public static final SizeMonitor INSTANCE = new SizeMonitor();
	}

	public static SizeMonitor getInstance() {
		return SizeHolder.INSTANCE;
	}

	private SizeMonitor() {
		if (enableMonitor) {
			init();
		}
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

	public static boolean isEnable() {
		return enableMonitor;
	}

	public void logSize(int size, String event, String source) {
		if (enableMonitor) {
			try {
				logSize(size, sizeRangeArray, event, source);
			} catch (Throwable t) {
				logger.warn("error while logging size:" + t.getMessage());
			}
		}
	}

	private void logSize(int size, int[] rangeArray, String eventName, String source) {
		if (rangeArray != null && rangeArray.length > 0) {
			String value = ">" + rangeArray[rangeArray.length - 1] + "k";
			int sizeK = (int) Math.ceil(size * 1d / 1024);
			if (rangeArray.length >= sizeK) {
				value = "<" + rangeArray[sizeK - 1] + "k";
			}
			monitor.logEvent(eventName, value, size + "");
		}
	}
}
