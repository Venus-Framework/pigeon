package com.dianping.pigeon.remoting.provider.process.statistics;

import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;

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
			Map<String, ProviderCapacityBucket> buckets = ProviderStatisticsHolder.getCapacityBuckets();
			if (buckets != null) {
				try {
					for (String key : buckets.keySet()) {
						ProviderCapacityBucket bucket = buckets.get(key);
						bucket.resetRequestsInSecondCounter();
					}
					if (++i % 12 == 0) {
						i = 0;
						for (ProviderCapacityBucket bucket : buckets.values()) {
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
