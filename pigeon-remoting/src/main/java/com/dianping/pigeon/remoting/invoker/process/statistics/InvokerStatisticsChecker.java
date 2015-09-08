package com.dianping.pigeon.remoting.invoker.process.statistics;

import java.util.Calendar;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.monitor.QpsMonitor;
import com.dianping.pigeon.remoting.common.util.Constants;

public class InvokerStatisticsChecker implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(InvokerStatisticsChecker.class);

	@Override
	public void run() {
		InvokerStatisticsHolder.init();
		InvokerCapacityBucket.init();
		int i = 0, j = 0;
		int maxQps = 0;
		String timeOfMaxQps = "";
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			Map<String, InvokerCapacityBucket> buckets = InvokerStatisticsHolder.getCapacityBuckets();
			if (buckets != null) {
				int totalQps = 0;
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, -1);
				int lastSecond = cal.get(Calendar.SECOND);
				String time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + lastSecond;
				try {
					i++;
					if (i % 5 == 0) {
						for (String key : buckets.keySet()) {
							InvokerCapacityBucket bucket = buckets.get(key);
							bucket.resetRequestsInSecondCounter();
						}
					}
					for (String key : buckets.keySet()) {
						InvokerCapacityBucket bucket = buckets.get(key);
						int qps = bucket.getRequestsInSecond(lastSecond);
						totalQps += qps;
					}
					if (totalQps > maxQps) {
						maxQps = totalQps;
						timeOfMaxQps = time;
					}
					if (i % Constants.QPS_INTERVAL == 0 && StringUtils.isNotBlank(timeOfMaxQps)) {
						QpsMonitor.getInstance().logQps("PigeonCall.QPS", maxQps, timeOfMaxQps);
						maxQps = 0;
						timeOfMaxQps = "";
					}

					if (i % 60 == 0) {
						i = 0;
						for (InvokerCapacityBucket bucket : buckets.values()) {
							bucket.resetRequestsInMinuteCounter();
						}
					}
					if (++j % 86400 == 0) {
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
