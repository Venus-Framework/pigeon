package com.dianping.pigeon.remoting.invoker.route.statistics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;

public final class ServiceStatisticsHolder {

	private static final Logger logger = LoggerLoader.getLogger(ServiceStatisticsHolder.class);

	private static ConcurrentHashMap<String, CapacityBucket> serverCapacityBuckets = new ConcurrentHashMap<String, CapacityBucket>();

	public static final boolean statEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.routestat.enable", true);

	public static float getCapacity(String server) {
		CapacityBucket barrel = serverCapacityBuckets.get(server);
		return barrel != null ? barrel.getCapacity() : 0f;
	}

	public static void init() {
	}

	public static Map<String, CapacityBucket> getCapacityBuckets() {
		return serverCapacityBuckets;
	}

	public static CapacityBucket getCapacityBucket(String server) {
		CapacityBucket barrel = serverCapacityBuckets.get(server);
		if (barrel == null) {
			CapacityBucket newBarrel = new CapacityBucket(server);
			barrel = serverCapacityBuckets.putIfAbsent(server, newBarrel);
			if (barrel == null) {
				barrel = newBarrel;
			}
		}
		return barrel;
	}

	public static void flowIn(InvocationRequest request, String toServer) {
		if (checkRequestNeedStat(request)) {
			CapacityBucket barrel = getCapacityBucket(toServer);
			if (barrel != null) {
				barrel.flowIn(request);
			} else {
				logger.error("Got a null barrel with server[" + toServer + "] in flowIn operation.");
			}
		}
	}

	public static void flowOut(InvocationRequest request, String fromServer) {
		if (checkRequestNeedStat(request)) {
			CapacityBucket barrel = getCapacityBucket(fromServer);
			if (barrel != null) {
				barrel.flowOut(request);
			} else {
				logger.error("Got a null barrel with server[" + fromServer + "] in flowOut operation.");
			}
		}
	}

	public static boolean checkRequestNeedStat(InvocationRequest request) {
		if (request == null || request.getMessageType() != Constants.MESSAGE_TYPE_SERVICE) {
			return false;
		}
		return statEnable;
	}

	public static void removeCapacityBucket(String server) {
		serverCapacityBuckets.remove(server);
	}
}
