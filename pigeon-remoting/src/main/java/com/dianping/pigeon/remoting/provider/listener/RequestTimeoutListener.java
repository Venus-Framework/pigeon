/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.listener;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.context.ThreadLocalInfo;
import com.dianping.pigeon.context.ThreadLocalUtils;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.exception.ProcessTimeoutException;
import com.dianping.pigeon.remoting.provider.exception.RequestAbortedException;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;
import com.dianping.pigeon.util.ContextUtils;

public class RequestTimeoutListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(RequestTimeoutListener.class);
	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(Monitor.class).getLogger();
	private Map<InvocationRequest, ProviderContext> requestContextMap;
	private RequestProcessor requestProcessor;
	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private long timeoutInterval = configManager.getLongValue(Constants.KEY_TIMEOUT_INTERVAL,
			Constants.DEFAULT_TIMEOUT_INTERVAL);
	private boolean defaultCancelTimeout = configManager.getBooleanValue(Constants.KEY_TIMEOUT_CANCEL,
			Constants.DEFAULT_TIMEOUT_CANCEL);
	private boolean interruptBusy = configManager.getBooleanValue("pigeon.timeout.interruptbusy", true);

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

	public void run() {
		Map<String, Server> servers = ProviderBootStrap.getServersMap();
		RequestProcessor processor = null;
		for (Server server : servers.values()) {
			if (Constants.PROTOCOL_DEFAULT.equals(server.getProtocol())) {
				processor = server.getRequestProcessor();
			}
		}
		while (true) {
			try {
				Thread.sleep(timeoutInterval);
				long currentTime = System.currentTimeMillis();
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
									StringBuilder msg = new StringBuilder();
									msg.append("timeout while processing request, from:")
											.append(rc.getChannel() == null ? "" : rc.getChannel().getRemoteAddress())
											.append(", to:")
											.append(ExtensionLoader.getExtension(ConfigManager.class).getLocalIp())
											.append(", process time:").append(System.currentTimeMillis())
											.append("\r\nrequest:").append(InvocationUtils.toJsonString(request))
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
									logger.error(te.getMessage(), te);
									if (monitorLogger != null) {
										monitorLogger.logError(te);
									}
									Future<?> future = rc.getFuture();
									if (t != null && !needInterrupt(t)) {
										cancelTimeout = false;
									}
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
				TimelineManager.removeLegacyTimelines();
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private boolean needInterrupt(Thread t) {
		try {
			Field field1 = Thread.class.getDeclaredField("threadLocals");
			field1.setAccessible(true);
			Object o1 = field1.get(t);
			Field field2 = o1.getClass().getDeclaredField("table");
			field2.setAccessible(true);
			Object[] o2 = (Object[]) field2.get(o1);
			for (Object temp : o2) {
				if (temp != null) {
					Field field3 = temp.getClass().getDeclaredField("value");
					field3.setAccessible(true);
					Object o3 = field3.get(temp);
					if (o3 instanceof ThreadLocalInfo) {
						return ThreadLocalUtils.canInterrupt((ThreadLocalInfo) o3);
					}
				}
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		return true;
	}
}
