package com.dianping.pigeon.remoting.provider.process.statistics;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;

public class ProviderStatisticsChecker implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(ProviderStatisticsChecker.class);

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			if (ProviderStatisticsHolder.getCapacityBuckets() != null) {
				try {
					for (ProviderCapacityBucket bucket : ProviderStatisticsHolder.getCapacityBuckets().values()) {
						bucket.resetRequestsInSecondCounter();
					}
				} catch (Throwable e) {
					logger.error("Check expired request in app statistics failed, detail[" + e.getMessage() + "].", e);
				}
			}
		}
	}

}
