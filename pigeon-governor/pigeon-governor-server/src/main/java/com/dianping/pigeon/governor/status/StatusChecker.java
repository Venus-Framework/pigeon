package com.dianping.pigeon.governor.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatusChecker implements Runnable {

	private static final Logger logger = LogManager.getLogger(StatusChecker.class);
	public static final boolean logEnable = true;

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			if (StatusHolder.getCapacityBuckets() != null) {
				try {
					for (String key : StatusHolder.getCapacityBuckets().keySet()) {
						CapacityBucket bucket = StatusHolder.getCapacityBuckets().get(key);
						if (logEnable && bucket.getRequestsInLastSecond() > 0) {
							System.out.println(key + "-" + bucket);
						}
						bucket.resetRequestsInSecondCounter();
					}
				} catch (Throwable e) {
					logger.error("Check expired request in app statistics failed, detail[" + e.getMessage() + "].", e);
				}
			}
		}
	}

}
