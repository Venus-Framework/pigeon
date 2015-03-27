package com.dianping.pigeon.remoting.invoker.process.statistics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;

public final class InvokerStatisticsHolder {

	private static final Logger logger = LoggerLoader.getLogger(InvokerStatisticsHolder.class);

	private static ConcurrentHashMap<String, InvokerCapacityBucket> appCapacityBuckets = new ConcurrentHashMap<String, InvokerCapacityBucket>();

	public static final boolean statEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.invokerstat.enable", true);

	public static void init() {
	}
	
	public static Map<String, InvokerCapacityBucket> getCapacityBuckets() {
		return appCapacityBuckets;
	}

	public static InvokerCapacityBucket getCapacityBucket(InvocationRequest request) {
		String fromApp = request.getApp();
		if (fromApp == null) {
			fromApp = "";
		}
		InvokerCapacityBucket barrel = appCapacityBuckets.get(fromApp);
		if (barrel == null) {
			InvokerCapacityBucket newBarrel = new InvokerCapacityBucket(fromApp);
			barrel = appCapacityBuckets.putIfAbsent(fromApp, newBarrel);
			if (barrel == null) {
				barrel = newBarrel;
			}
		}
		return barrel;
	}

	public static void flowIn(InvocationRequest request) {
		if (checkRequestNeedStat(request)) {
			InvokerCapacityBucket barrel = getCapacityBucket(request);
			if (barrel != null) {
				barrel.flowIn(request);
			}
		}
	}

	public static void flowOut(InvocationRequest request) {
		if (checkRequestNeedStat(request)) {
			InvokerCapacityBucket barrel = getCapacityBucket(request);
			if (barrel != null) {
				barrel.flowOut(request);
			}
		}
	}

	public static boolean checkRequestNeedStat(InvocationRequest request) {
		if (request == null || request.getMessageType() != Constants.MESSAGE_TYPE_SERVICE) {
			return false;
		}
		return statEnable;
	}

	public static void removeCapacityBucket(String fromApp) {
		appCapacityBuckets.remove(fromApp);
	}
}
