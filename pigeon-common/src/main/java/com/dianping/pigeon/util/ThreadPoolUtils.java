package com.dianping.pigeon.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtils {

	public static void shutdown(ExecutorService executor) {
		if (executor != null) {
			try {
				executor.shutdown();
				executor.awaitTermination(5, TimeUnit.SECONDS);
				executor = null;
			} catch (InterruptedException e) {
			}
		}
	}
}
