package com.dianping.pigeon.remoting.invoker.route.statistics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;

public class CapacityChecker implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(CapacityChecker.class);

	@Override
	public void run() {
		ServiceStatisticsHolder.init();
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			if (ServiceStatisticsHolder.getCapacityBuckets() != null) {
				try {
					long currentTimeMillis = System.currentTimeMillis();
					for (CapacityBucket bucket : ServiceStatisticsHolder.getCapacityBuckets().values()) {
						bucket.resetRequestInSecondCounter();
						try {
							Map<Long, Float> expiredRequests = new HashMap<Long, Float>();
							for (Iterator<Entry<Long, Object[]>> iter = bucket.requestSeqDetails.entrySet().iterator(); iter
									.hasNext();) {
								Entry<Long, Object[]> detailEntry = iter.next();
								Object[] details = detailEntry.getValue();
								long requestFlowInTime = (Long) details[0];
								int requestTimeout = (Integer) details[1];
								Float requestFlow = (Float) details[2];
								if (currentTimeMillis - requestFlowInTime >= 2 * requestTimeout) {
									expiredRequests.put(detailEntry.getKey(), requestFlow);
								}
							}
							for (Entry<Long, Float> expiredEntry : expiredRequests.entrySet()) {
								bucket.flowOut(expiredEntry.getKey(), expiredEntry.getValue());
							}
						} catch (Throwable e) {
							logger.error("Check expired request in service statistics failed, detail[" + e.getMessage()
									+ "].", e);
						}
					}
				} catch (Throwable e) {
					logger.error("Check expired request in service statistics failed, detail[" + e.getMessage() + "].",
							e);
				}
			}
		}
	}

}
