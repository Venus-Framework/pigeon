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

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.dianping.pigeon.config.ConfigChangeListener;
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
	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static long timeoutInterval = configManager.getLongValue(Constants.KEY_TIMEOUT_INTERVAL,
			Constants.DEFAULT_TIMEOUT_INTERVAL);
	private static boolean defaultCancelTimeout = configManager.getBooleanValue(Constants.KEY_TIMEOUT_CANCEL,
			Constants.DEFAULT_TIMEOUT_CANCEL);
	private static boolean interruptBusy = configManager.getBooleanValue("pigeon.timeout.interruptbusy", true);
	private static int requestQueueSize = configManager.getIntValue("pigeon.timeout.requestqueue.size", 10);
	private Queue<Map<String, Integer>> timeoutRequestQueue = new ArrayBlockingQueue<Map<String, Integer>>(
			requestQueueSize);
	private volatile Map<String, Integer> timeoutRequestCountMap = null;
	private static int timeoutRequestThreshold = configManager.getIntValue("pigeon.timeout.request.thredhold", 3);
	private static boolean isolatedByApp = configManager.getBooleanValue("pigeon.timeout.isolation.app", true);
	private static boolean isolatedByParameters = configManager.getBooleanValue("pigeon.timeout.isolation.parameters",
			false);

	private class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith("pigeon.timeout.interruptbusy")) {
				try {
					interruptBusy = Boolean.valueOf(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith("pigeon.timeout.cancel")) {
				try {
					defaultCancelTimeout = Boolean.valueOf(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith("pigeon.timeout.request.thredhold")) {
				try {
					timeoutRequestThreshold = Integer.valueOf(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith("pigeon.timeout.isolation.app")) {
				try {
					isolatedByApp = Boolean.valueOf(value);
				} catch (RuntimeException e) {
				}
			}  else if (key.endsWith("pigeon.timeout.isolation.parameters")) {
				try {
					isolatedByParameters = Boolean.valueOf(value);
				} catch (RuntimeException e) {
				}
			} 
		}

		@Override
		public void onKeyAdded(String key, String value) {
		}

		@Override
		public void onKeyRemoved(String key) {
		}

	}

	public RequestTimeoutListener(RequestProcessor requestProcessor,
			Map<InvocationRequest, ProviderContext> requestContextMap) {
		this.requestProcessor = requestProcessor;
		this.requestContextMap = requestContextMap;
		configManager.registerConfigChangeListener(new InnerConfigChangeListener());
	}

	private String getRequestUrl(InvocationRequest request) {
		StringBuilder url = new StringBuilder();
		url.append(request.getServiceName()).append("#").append(request.getMethodName());
		if (isolatedByParameters) {
			url.append("#").append(StringUtils.join(request.getParamClassName(), ","));
		}
		if (isolatedByApp) {
			url.append("#").append(request.getApp());
		}
		return url.toString();
	}

	public boolean isSlowRequest(InvocationRequest request) {
		if (!CollectionUtils.isEmpty(timeoutRequestCountMap)) {
			String requestUrl = getRequestUrl(request);
			Integer total = timeoutRequestCountMap.get(requestUrl);
			if (total != null && total > timeoutRequestThreshold) {
				return true;
			}
		}

		return false;
	}

	public void run() {
		Map<String, Server> servers = ProviderBootStrap.getServersMap();
		RequestProcessor processor = null;
		for (Server server : servers.values()) {
			if (Constants.PROTOCOL_DEFAULT.equals(server.getProtocol())) {
				processor = server.getRequestProcessor();
			}
		}
		int timeoutCountInLastSecond = 0;
		int timeoutCountInCurrentSecond = 0;
		while (true) {
			timeoutCountInLastSecond = timeoutCountInCurrentSecond;
			timeoutCountInCurrentSecond = 0;
			try {
				Thread.sleep(timeoutInterval);
				if (timeoutRequestQueue.size() >= requestQueueSize) {
					Map<String, Integer> reqMap = timeoutRequestQueue.poll();
					if (reqMap != null) {
						reqMap.clear();
					}
				}
				long currentTime = System.currentTimeMillis();
				Map<String, Integer> timeoutRequests = new HashMap<String, Integer>();
				for (InvocationRequest request : requestContextMap.keySet()) {
					if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
							&& (request.getCreateMillisTime() + request.getTimeout()) < currentTime) {
						try {
							ProviderContext rc = requestContextMap.get(request);
							if (rc != null) {
								boolean cancelTimeout = defaultCancelTimeout;
								if (interruptBusy && processor != null) {
									cancelTimeout = processor.needCancelRequest(request);
								}
								if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
									Future<?> future = rc.getFuture();
									if (future != null && !future.isCancelled()) {
										future.cancel(cancelTimeout);
									}
								} else {
									timeoutCountInCurrentSecond++;
									String requestUrl = getRequestUrl(request);
									Integer timeoutCount = timeoutRequests.get(requestUrl);
									if (timeoutCount == null) {
										timeoutRequests.put(requestUrl, 1);
									} else {
										timeoutRequests.put(requestUrl, timeoutCount + 1);
									}
									StringBuilder msg = new StringBuilder();
									msg.append("timeout while processing request, from:")
											.append(rc.getChannel() == null ? "" : rc.getChannel().getRemoteAddress())
											.append(", to:")
											.append(ConfigManagerLoader.getConfigManager().getLocalIp())
											.append(", process time:").append(System.currentTimeMillis())
											.append("\r\nrequest:").append(request)
											.append("\r\nprocessor stats:interrupt:").append(cancelTimeout).append(",")
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
									ContextUtils.setContext(request.getContext());
									boolean isLog = true;
									if (timeoutCountInLastSecond > Constants.LOG_THRESHOLD && timeoutCountInCurrentSecond % Constants.LOG_INTERVAL == 1) {
										isLog = false;
									}
									if (isLog) {
										logger.error(te.getMessage(), te);
									}
									if (monitor != null) {
										monitor.logError(te);
									}
									Future<?> future = rc.getFuture();
									if (future != null && !future.isCancelled()) {
										if (future.cancel(cancelTimeout)) {
										}
									}
								}
							} else {
								logger.error("provider context is null with request:" + request);
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
		Map<String, Integer> countMap = null;
		Iterator<Map<String, Integer>> ir = timeoutRequestQueue.iterator();
		while (ir.hasNext()) {
			Map<String, Integer> element = ir.next();
			for (String key : element.keySet()) {
				Integer count = element.get(key);
				if (countMap == null) {
					countMap = new ConcurrentHashMap<String, Integer>();
				}
				Integer total = countMap.get(key);
				if (total == null) {
					countMap.put(key, count);
				} else {
					countMap.put(key, total + count);
				}
			}
		}
		timeoutRequestCountMap = countMap;
	}

}
