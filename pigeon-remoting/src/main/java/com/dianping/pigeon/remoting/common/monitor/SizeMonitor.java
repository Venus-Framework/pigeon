package com.dianping.pigeon.remoting.common.monitor;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;

public class SizeMonitor {

	private static Logger logger = LoggerLoader.getLogger(SizeMonitor.class);

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

	public String getLogSize(int size) {
		if (size > sizeMin) {
			try {
				return getLogSize(size, sizeRangeArray);
			} catch (Throwable t) {
				logger.warn("error while logging size:" + t.getMessage());
			}
		}
		return null;
	}

	private String getLogSize(int size, int[] rangeArray) {
		if (size > 0 && rangeArray != null && rangeArray.length > 0) {
			String value = ">" + rangeArray[rangeArray.length - 1] + "k";
			int sizeK = (int) Math.ceil(size * 1d / 1024);
			if (rangeArray.length > sizeK) {
				value = "<" + rangeArray[sizeK] + "k";
			}
			return value;
		}
		return null;
	}
}
