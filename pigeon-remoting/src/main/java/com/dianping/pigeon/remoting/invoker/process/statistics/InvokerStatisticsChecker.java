package com.dianping.pigeon.remoting.invoker.process.statistics;

import java.util.Map;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.log.LoggerLoader;

public class InvokerStatisticsChecker implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(InvokerStatisticsChecker.class);

	@Override
	public void run() {
		InvokerStatisticsHolder.init();
		InvokerCapacityBucket.init();
		int i = 0, j = 0;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			Map<String, InvokerCapacityBucket> buckets = InvokerStatisticsHolder.getCapacityBuckets();
			if (buckets != null) {
				try {
					for (String key : buckets.keySet()) {
						InvokerCapacityBucket bucket = buckets.get(key);
						bucket.resetRequestsInSecondCounter();
					}
					if (++i % 12 == 0) {
						i = 0;
						for (InvokerCapacityBucket bucket : buckets.values()) {
							bucket.resetRequestsInMinuteCounter();
						}
					}
					if (++j % 17280 == 0) {
						j = 0;
						for (InvokerCapacityBucket bucket : buckets.values()) {
							bucket.resetRequestsInDayCounter();
						}
					}
				} catch (Throwable e) {
					logger.error("Check expired request in app statistics failed, detail[" + e.getMessage() + "].", e);
				}
			}
		}
	}

}
