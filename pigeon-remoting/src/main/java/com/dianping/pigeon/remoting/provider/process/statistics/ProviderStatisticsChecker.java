package com.dianping.pigeon.remoting.provider.process.statistics;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

public class ProviderStatisticsChecker implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(ProviderStatisticsChecker.class);

	@Override
	public void run() {
		ProviderStatisticsHolder.init();
		ProviderCapacityBucket.init();
		int i = 0;
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
					if (++i % 12 == 0) {
						i = 0;
						for (ProviderCapacityBucket bucket : ProviderStatisticsHolder.getCapacityBuckets().values()) {
							bucket.resetRequestsInMinuteCounter();
						}
					}
				} catch (Throwable e) {
					logger.error("Check expired request in app statistics failed, detail[" + e.getMessage() + "].", e);
				}
			}
		}
	}

}
