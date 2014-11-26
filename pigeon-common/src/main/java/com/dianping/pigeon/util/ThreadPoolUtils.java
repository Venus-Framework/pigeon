package com.dianping.pigeon.util;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtils {

	public static void shutdown(ThreadPoolExecutor executor) {
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
