package com.dianping.pigeon.remoting.provider.process.statistics;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;

public class AppStatisticsChecker implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(AppStatisticsChecker.class);

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			if (AppStatisticsHolder.getCapacityBuckets() != null) {
				try {
					for (AppCapacityBucket bucket : AppStatisticsHolder.getCapacityBuckets().values()) {
						bucket.resetRequestsInSecondCounter();
					}
				} catch (Throwable e) {
					logger.error("Check expired request in app statistics failed, detail[" + e.getMessage() + "].", e);
				}
			}
		}
	}

}
