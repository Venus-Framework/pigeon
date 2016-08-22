/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.generic.UnifiedRequest;
import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.Logger;
import org.springframework.util.CollectionUtils;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.exception.ProcessTimeoutException;
import com.dianping.pigeon.remoting.provider.exception.RequestAbortedException;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;
import com.dianping.pigeon.util.ContextUtils;

public class RequestTimeoutListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(RequestTimeoutListener.class);
	private static final Monitor monitor = MonitorLoader.getMonitor();
	private Map<InvocationRequest, ProviderContext> requestContextMap;
	private RequestProcessor requestProcessor;
	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static int requestQueueSize = configManager.getIntValue("pigeon.provider.timeout.requestqueue.size", 100);
	private Queue<Map<String, Count>> timeoutRequestQueue = new ArrayBlockingQueue<Map<String, Count>>(
			requestQueueSize);
	private volatile Map<String, Count> timeoutRequestCountMap = null;
	private static final String KEY_TIMEOUT_SLOW_PCT_THRESHOLD = "pigeon.provider.timeout.slow.pct.threshold";
	private static final String KEY_TIMEOUT_SLOW_COUNT_THRESHOLD = "pigeon.provider.timeout.slow.count.threshold";
	private static final String KEY_TIMEOUT_ISOLATION_APP = "pigeon.provider.timeout.isolation.app";
	private static final String KEY_TIMEOUT_ISOLATION_PARAMETERS = "pigeon.provider.timeout.isolation.parameters";
	private static final String KEY_TIMEOUT_CANCEL = "pigeon.provider.timeout.cancel";
	private static final String KEY_TIMEOUT_INTERRUPT = "pigeon.provider.timeout.interruptbusy";
	private static final String KEY_TIMEOUT_INTERVAL = "pigeon.provider.timeout.interval";

	public RequestTimeoutListener(RequestProcessor requestProcessor,
			Map<InvocationRequest, ProviderContext> requestContextMap) {
		this.requestProcessor = requestProcessor;
		this.requestContextMap = requestContextMap;
		configManager.getIntValue(KEY_TIMEOUT_INTERVAL, 100);
		configManager.getFloatValue(KEY_TIMEOUT_SLOW_PCT_THRESHOLD, 5);
		configManager.getIntValue(KEY_TIMEOUT_SLOW_COUNT_THRESHOLD, 300);
		configManager.getBooleanValue(KEY_TIMEOUT_ISOLATION_APP, true);
		configManager.getBooleanValue(KEY_TIMEOUT_ISOLATION_PARAMETERS, false);
		configManager.getBooleanValue(KEY_TIMEOUT_CANCEL, Constants.DEFAULT_TIMEOUT_CANCEL);
		configManager.getBooleanValue(KEY_TIMEOUT_INTERRUPT, true);
	}

	private String getRequestUrl(InvocationRequest request) {
		StringBuilder url = new StringBuilder();
		url.append(request.getServiceName()).append("#").append(request.getMethodName());
		if (configManager.getBooleanValue(KEY_TIMEOUT_ISOLATION_PARAMETERS, false)) {
			url.append("#").append(StringUtils.join(request.getParamClassName(), ","));
		}
		if (configManager.getBooleanValue(KEY_TIMEOUT_ISOLATION_APP, true)) {
			url.append("#").append(request.getApp());
		}
		return url.toString();
	}

	public boolean isSlowRequest(InvocationRequest request) {
		if (!CollectionUtils.isEmpty(timeoutRequestCountMap)) {
			String requestUrl = getRequestUrl(request);
			Count count = timeoutRequestCountMap.get(requestUrl);
			if (count != null && (count.getTimeoutPercent() >= configManager
					.getFloatValue(KEY_TIMEOUT_SLOW_PCT_THRESHOLD, 5)
					|| count.getTimeout() >= configManager.getIntValue(KEY_TIMEOUT_SLOW_COUNT_THRESHOLD, 300))) {
				return true;
			}
		}
		return false;
	}

	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
		}
		Map<String, Server> servers = ProviderBootStrap.getServersMap();
		RequestProcessor processor = null;
		for (Server server : servers.values()) {
			if (Constants.PROTOCOL_DEFAULT.equals(server.getProtocol())) {
				processor = server.getRequestProcessor();
			}
		}
		int i = 0;
		while (true) {
			int interval = configManager.getIntValue(KEY_TIMEOUT_INTERVAL, 100);
			try {
				Thread.sleep(interval);
				i++;
				if (timeoutRequestQueue.size() >= requestQueueSize) {
					Map<String, Count> reqMap = timeoutRequestQueue.poll();
					if (reqMap != null) {
						reqMap.clear();
					}
				}
				long currentTime = System.currentTimeMillis();
				Map<String, Count> timeoutRequests = new HashMap<String, Count>();
				for (InvocationRequest request : requestContextMap.keySet()) {
					String requestUrl = getRequestUrl(request);
					Count timeoutCount = timeoutRequests.get(requestUrl);
					if (timeoutCount == null) {
						timeoutCount = new Count();
						timeoutRequests.put(requestUrl, timeoutCount);
					}
					if (request.getMessageType() != Constants.MESSAGE_TYPE_HEART) {
						timeoutCount.incTotal();
					}
					if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
							&& (request.getCreateMillisTime() + request.getTimeout()) < currentTime) {
						try {
							ProviderContext rc = requestContextMap.get(request);
							if (rc != null) {
								boolean cancelTimeout = configManager.getBooleanValue(KEY_TIMEOUT_CANCEL,
										Constants.DEFAULT_TIMEOUT_CANCEL);
								if (configManager.getBooleanValue(KEY_TIMEOUT_INTERRUPT, true) && processor != null) {
									cancelTimeout = processor.needCancelRequest(request);
								}
								if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
									Future<?> future = rc.getFuture();
									if (future != null && !future.isCancelled()) {
										future.cancel(cancelTimeout);
									}
								} else {
									timeoutCount.incTimeout();
									if (i % (1000 / interval) == 0) {
										StringBuilder msg = new StringBuilder();
										msg.append("timeout while processing request, from:")
												.append(rc.getChannel() == null ? ""
														: rc.getChannel().getRemoteAddress())
												.append(", to:")
												.append(ConfigManagerLoader.getConfigManager().getLocalIp())
												.append(", process time:").append(System.currentTimeMillis())
												.append("\r\nrequest:").append(request)
												.append("\r\nprocessor stats:interrupt:").append(cancelTimeout)
												.append(",")
												.append(this.requestProcessor.getProcessorStatistics(request));
										Exception te = null;
										Thread t = rc.getThread();
										if (t == null) {
											msg.append("\r\nthe request has not been executed");
											te = new RequestAbortedException(msg.toString());
											te.setStackTrace(new StackTraceElement[] {});
										} else {
											te = new ProcessTimeoutException(msg.toString());
											te.setStackTrace(t.getStackTrace());
										}
										if (!(request instanceof UnifiedRequest)) {
											ContextUtils.setContext(request.getContext());
										}
										logger.error(te.getMessage(), te);
										if (monitor != null) {
											monitor.logError(te);
										}
									}
									Future<?> future = rc.getFuture();
									if (future != null && !future.isCancelled()) {
										if (future.cancel(cancelTimeout)) {
										}
									}
								}
							} else {
								// logger.error("provider context is null with
								// request:" + request);
							}
						} finally {
							requestContextMap.remove(request);
						}
					}
				}
				timeoutRequestQueue.offer(timeoutRequests);
				countTotalTimeoutRequests();
			} catch (Throwable e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	private void countTotalTimeoutRequests() {
		Map<String, Count> countMap = null;
		Iterator<Map<String, Count>> ir = timeoutRequestQueue.iterator();
		while (ir.hasNext()) {
			Map<String, Count> element = ir.next();
			for (String key : element.keySet()) {
				Count count = element.get(key);
				if (countMap == null) {
					countMap = new ConcurrentHashMap<String, Count>();
				}
				Count last = countMap.get(key);
				if (last == null) {
					countMap.put(key, count);
				} else {
					countMap.put(key, last.merge(count));
				}
			}
		}
		timeoutRequestCountMap = countMap;
	}

	private static class Count {
		private int total;
		private int timeout;

		public Count(int total, int timeout) {
			this.total = total;
			this.timeout = timeout;
		}

		public Count() {
		}

		public int getTotal() {
			return total;
		}

		public void setTotal(int total) {
			this.total = total;
		}

		public int getTimeout() {
			return timeout;
		}

		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}

		public float getTimeoutPercent() {
			if (total > 0) {
				return timeout * 100 / total;
			} else {
				return 0;
			}
		}

		public Count merge(Count count) {
			Count n = new Count();
			n.total = this.total + count.total;
			n.timeout = this.timeout + count.timeout;
			return n;
		}

		public void clear() {
			timeout = 0;
			total = 0;
		}

		public void incTotal() {
			total++;
		}

		public void incTimeout() {
			timeout++;
		}

		public String toString() {
			return timeout + "/" + total + "=" + getTimeoutPercent();
		}
	}
}
