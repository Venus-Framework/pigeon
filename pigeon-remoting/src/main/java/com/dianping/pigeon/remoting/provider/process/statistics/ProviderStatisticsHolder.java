package com.dianping.pigeon.remoting.provider.process.statistics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;

public final class ProviderStatisticsHolder {

	private static final Logger logger = LoggerLoader.getLogger(ProviderStatisticsHolder.class);

	private static ConcurrentHashMap<String, ProviderCapacityBucket> appCapacityBuckets = new ConcurrentHashMap<String, ProviderCapacityBucket>();

	public static final boolean statEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.providerstat.enable", true);

	public static void init() {
	}
	
	public static Map<String, ProviderCapacityBucket> getCapacityBuckets() {
		return appCapacityBuckets;
	}

	public static ProviderCapacityBucket getCapacityBucket(InvocationRequest request) {
		String fromApp = request.getApp();
		if (fromApp == null) {
			fromApp = "";
		}
		ProviderCapacityBucket barrel = appCapacityBuckets.get(fromApp);
		if (barrel == null) {
			ProviderCapacityBucket newBarrel = new ProviderCapacityBucket(fromApp);
			barrel = appCapacityBuckets.putIfAbsent(fromApp, newBarrel);
			if (barrel == null) {
				barrel = newBarrel;
			}
		}
		return barrel;
	}

	public static void flowIn(InvocationRequest request) {
		if (checkRequestNeedStat(request)) {
			ProviderCapacityBucket barrel = getCapacityBucket(request);
			if (barrel != null) {
				barrel.flowIn(request);
			}
		}
	}

	public static void flowOut(InvocationRequest request) {
		if (checkRequestNeedStat(request)) {
			ProviderCapacityBucket barrel = getCapacityBucket(request);
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
