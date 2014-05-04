/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.console.listener;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;

public class NotifyFailureListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(NotifyFailureListener.class);
	private static final int MAX_RETRIES = 5;
	private static final int CHECK_INTERVAL = 5000;
	private DefaultServiceChangeListener serviceChangeListener = null;

	public NotifyFailureListener(DefaultServiceChangeListener serviceChangeListener) {
		this.serviceChangeListener = serviceChangeListener;
	}

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			Map<String, NotifyEvent> failedNotifyEvents = new HashMap<String, NotifyEvent>();
			failedNotifyEvents.putAll(serviceChangeListener.getFailedNotifyEvents());
			try {
				for (String serviceUrl : failedNotifyEvents.keySet()) {
					if (serviceChangeListener.getFailedNotifyEvents().containsKey(serviceUrl)) {
						NotifyEvent notifyEvent = failedNotifyEvents.get(serviceUrl);
						boolean isSuccess = serviceChangeListener.doNotify(notifyEvent.getNotifyUrl());
						notifyEvent.setRetries(notifyEvent.getRetries() + 1);
						if (isSuccess) {
							serviceChangeListener.getFailedNotifyEvents().remove(serviceUrl);
						} else if (notifyEvent.getRetries() >= MAX_RETRIES) {
							serviceChangeListener.getFailedNotifyEvents().remove(serviceUrl);
							logger.warn("Reached max retries while notifying to " + notifyEvent.getNotifyUrl());
						}
					}
				}
				Thread.sleep(CHECK_INTERVAL);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
