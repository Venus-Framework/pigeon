package com.dianping.pigeon.remoting.invoker.process.statistics;

import org.apache.log4j.Logger;

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
			if (InvokerStatisticsHolder.getCapacityBuckets() != null) {
				try {
					for (InvokerCapacityBucket bucket : InvokerStatisticsHolder.getCapacityBuckets().values()) {
						bucket.resetRequestsInSecondCounter();
					}
					if (++i % 12 == 0) {
						i = 0;
						for (InvokerCapacityBucket bucket : InvokerStatisticsHolder.getCapacityBuckets().values()) {
							bucket.resetRequestsInMinuteCounter();
						}
					}
					if (++j % 17280 == 0) {
						j = 0;
						for (InvokerCapacityBucket bucket : InvokerStatisticsHolder.getCapacityBuckets().values()) {
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
