package com.dianping.pigeon.remoting.invoker.route.stat.barrel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.dianping.dpsf.component.DPSFRequest;
import com.dianping.pigeon.remoting.common.util.Constants;

public final class ServerStatBarrelsHolder {

	private static final Logger logger = Logger.getLogger(ServerStatBarrelsHolder.class);
	public static ConcurrentMap<String, ServiceBarrel> serverStatBarrels = new ConcurrentHashMap<String, ServiceBarrel>();

	public static float getCapacity(String server) {
		ServiceBarrel barrel = serverStatBarrels.get(server);
		return barrel != null ? barrel.getCapacity() : 0f;
	}

	private static ServiceBarrel getServerBarrel(String server) {
		ServiceBarrel barrel = serverStatBarrels.get(server);
		if (barrel == null) {
			ServiceBarrel newBarrel = new ServiceBarrel(server);
			barrel = serverStatBarrels.putIfAbsent(server, newBarrel);
			if (barrel == null) {
				barrel = newBarrel;
			}
		}
		return barrel;
	}

	public static void flowIn(DPSFRequest request, String toServer) {
		if (checkRequestNeedStat(request)) {
			ServiceBarrel barrel = getServerBarrel(toServer);
			if (barrel != null) {
				barrel.flowIn(request);
			} else {
				logger.error("Got a null barrel with server[" + toServer + "] in flowIn operation.");
			}
		}
	}

	public static void flowOut(DPSFRequest request, String fromServer) {
		if (checkRequestNeedStat(request)) {
			ServiceBarrel barrel = getServerBarrel(fromServer);
			if (barrel != null) {
				barrel.flowOut(request);
			} else {
				logger.error("Got a null barrel with server[" + fromServer + "] in flowOut operation.");
			}
		}
	}

	private static boolean checkRequestNeedStat(DPSFRequest request) {
		return request != null && request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE;
	}
}
