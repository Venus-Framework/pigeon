package com.dianping.pigeon.remoting.invoker.route.statistics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadAutoawareLoadBalance;

public final class ServiceStatisticsHolder {

	private static final Logger logger = LoggerLoader.getLogger(ServiceStatisticsHolder.class);
	public static ConcurrentMap<String, ServiceStatistics> serverStatBarrels = new ConcurrentHashMap<String, ServiceStatistics>();

	public static float getCapacity(String server) {
		ServiceStatistics barrel = serverStatBarrels.get(server);
		return barrel != null ? barrel.getCapacity() : 0f;
	}

	private static ServiceStatistics getServerBarrel(String server) {
		ServiceStatistics barrel = serverStatBarrels.get(server);
		if (barrel == null) {
			ServiceStatistics newBarrel = new ServiceStatistics(server);
			barrel = serverStatBarrels.putIfAbsent(server, newBarrel);
			if (barrel == null) {
				barrel = newBarrel;
			}
		}
		return barrel;
	}

	public static void flowIn(InvocationRequest request, String toServer) {
		if (checkRequestNeedStat(request)) {
			ServiceStatistics barrel = getServerBarrel(toServer);
			if (barrel != null) {
				barrel.flowIn(request);
			} else {
				logger.error("Got a null barrel with server[" + toServer + "] in flowIn operation.");
			}
		}
	}

	public static void flowOut(InvocationRequest request, String fromServer) {
		if (checkRequestNeedStat(request)) {
			ServiceStatistics barrel = getServerBarrel(fromServer);
			if (barrel != null) {
				barrel.flowOut(request);
			} else {
				logger.error("Got a null barrel with server[" + fromServer + "] in flowOut operation.");
			}
		}
	}

	private static boolean checkRequestNeedStat(InvocationRequest request) {
		return request != null && request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE
				&& LoadAutoawareLoadBalance.NAME.equals(request.getLoadbalance());
	}
}
