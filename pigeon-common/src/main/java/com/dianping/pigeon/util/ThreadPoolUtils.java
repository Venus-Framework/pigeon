package com.dianping.pigeon.util;

import java.util.concurrent.ExecutorService;

public class ThreadPoolUtils {

	public static void shutdown(ExecutorService executor) {
		if (executor != null) {
			executor.shutdownNow();
			// executor.awaitTermination(1, TimeUnit.SECONDS);
			executor = null;
		}
	}
}
