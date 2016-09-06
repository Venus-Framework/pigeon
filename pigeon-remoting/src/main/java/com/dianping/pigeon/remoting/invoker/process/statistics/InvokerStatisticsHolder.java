package com.dianping.pigeon.remoting.invoker.process.statistics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;

public final class InvokerStatisticsHolder {

	private static final Logger logger = LoggerLoader.getLogger(InvokerStatisticsHolder.class);

	private static ConcurrentHashMap<String, InvokerCapacityBucket> appCapacityBuckets = new ConcurrentHashMap<String, InvokerCapacityBucket>();

	public static void init() {
	}

	public static Map<String, InvokerCapacityBucket> getCapacityBuckets() {
		return appCapacityBuckets;
	}

	public static InvokerCapacityBucket getCapacityBucket(InvocationRequest request, String targetApp) {
		String toApp = targetApp;
		if (toApp == null) {
			toApp = "";
		}
		InvokerCapacityBucket barrel = appCapacityBuckets.get(toApp);
		if (barrel == null) {
			InvokerCapacityBucket newBarrel = new InvokerCapacityBucket(toApp);
			barrel = appCapacityBuckets.putIfAbsent(toApp, newBarrel);
			if (barrel == null) {
				barrel = newBarrel;
			}
		}
		return barrel;
	}

	public static void flowIn(InvocationRequest request, String targetApp) {
		if (checkRequestNeedStat(request)) {
			InvokerCapacityBucket barrel = getCapacityBucket(request, targetApp);
			if (barrel != null) {
				barrel.flowIn(request);
			}
		}
	}

	public static void flowOut(InvocationRequest request, String targetApp) {
		if (checkRequestNeedStat(request)) {
			InvokerCapacityBucket barrel = getCapacityBucket(request, targetApp);
			if (barrel != null) {
				barrel.flowOut(request);
			}
		}
	}

	public static boolean checkRequestNeedStat(InvocationRequest request) {
		if (request == null || request.getMessageType() != Constants.MESSAGE_TYPE_SERVICE) {
			return false;
		}
		return true;
	}

	public static void removeCapacityBucket(String fromApp) {
		appCapacityBuckets.remove(fromApp);
	}
}
