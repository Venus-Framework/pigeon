package com.dianping.pigeon.test.benchmark.status;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StatusHolder {

	private static final Logger logger = LoggerFactory.getLogger(StatusHolder.class);

	private static ConcurrentHashMap<String, CapacityBucket> capacityBuckets = new ConcurrentHashMap<String, CapacityBucket>();

	public static void init() {
		Thread t = new Thread(new StatusChecker());
		t.setDaemon(true);
		t.start();
	}

	public static Map<String, CapacityBucket> getCapacityBuckets() {
		return capacityBuckets;
	}

	public static CapacityBucket getCapacityBucket(String source) {
		CapacityBucket barrel = capacityBuckets.get(source);
		if (barrel == null) {
			CapacityBucket newBarrel = new CapacityBucket(source);
			barrel = capacityBuckets.putIfAbsent(source, newBarrel);
			if (barrel == null) {
				barrel = newBarrel;
			}
		}
		return barrel;
	}

	public static void flowIn(String source) {
		if (checkRequestNeedStat(source)) {
			CapacityBucket barrel = getCapacityBucket(source);
			if (barrel != null) {
				barrel.flowIn();
			}
		}
	}

	public static void flowOut(String source) {
		if (checkRequestNeedStat(source)) {
			CapacityBucket barrel = getCapacityBucket(source);
			if (barrel != null) {
				barrel.flowOut();
			}
		}
	}

	public static boolean checkRequestNeedStat(String source) {
		return true;
	}

	public static void removeCapacityBucket(String source) {
		capacityBuckets.remove(source);
	}
}
