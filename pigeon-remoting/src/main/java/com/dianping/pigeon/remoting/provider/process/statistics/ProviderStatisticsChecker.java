package com.dianping.pigeon.remoting.provider.process.statistics;

import java.util.Calendar;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.monitor.QpsMonitor;
import com.dianping.pigeon.remoting.common.util.Constants;

public class ProviderStatisticsChecker implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(ProviderStatisticsChecker.class);


	@Override
	public void run() {
		ProviderStatisticsHolder.init();
		ProviderCapacityBucket.init();
		int i = 0;
		int maxQps = 0;
		String timeOfMaxQps = "";
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			Map<String, ProviderCapacityBucket> buckets = ProviderStatisticsHolder.getCapacityBuckets();
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
							ProviderCapacityBucket bucket = buckets.get(key);
							bucket.resetRequestsInSecondCounter();
						}
					}
					for (String key : buckets.keySet()) {
						ProviderCapacityBucket bucket = buckets.get(key);
						int qps = bucket.getRequestsInSecond(lastSecond);
						totalQps += qps;
					}
					if (totalQps > maxQps) {
						maxQps = totalQps;
						timeOfMaxQps = time;
					}
					if (i % Constants.QPS_INTERVAL == 0 && StringUtils.isNotBlank(timeOfMaxQps)) {
						QpsMonitor.getInstance().logQps("PigeonService.QPS", maxQps, timeOfMaxQps);
						maxQps = 0;
						timeOfMaxQps = "";
					}
					
					if (i % 60 == 0) {
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
