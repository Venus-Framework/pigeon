/**
 * Dianpingg.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;

/**
 * 默认的事件分发服务，支持同步和异步的模式。
 * 
 * @author jianhuihuang
 * 
 */
public class EventManager {

	private static final Logger log = LoggerLoader.getLogger(EventManager.class);

	private final Collection<RuntimeServiceListener> listeners = Collections
			.synchronizedCollection(new ArrayList<RuntimeServiceListener>());

	private final Object monitor = new Object();

	private final static BlockingQueue<RuntimeServiceEvent> eventQueue = new LinkedBlockingQueue<RuntimeServiceEvent>();

	private Thread asynchronizeEventProcessor;

	private static EventManager instance = new EventManager();

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public static boolean IS_EVENT_ENABLED = configManager.getBooleanValue("pigeon.event.enabled", false);

	private EventManager() {
		asynchronizeEventProcessor = new Thread(new AsynchronizeEventProcessor(), "Pigeon-Asynchronize-Event-Processor");
		asynchronizeEventProcessor.setDaemon(true);
		asynchronizeEventProcessor.start();
	}

	public static EventManager getInstance() {
		return instance;
	}

	private class AsynchronizeEventProcessor implements Runnable {

		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				RuntimeServiceEvent event = null;
				try {
					event = eventQueue.take();
					synchronized (monitor) {
						// FIXME java.util.ConcurrentModificationException
						List<RuntimeServiceListener> arrayList = new ArrayList<RuntimeServiceListener>();
						arrayList.addAll(listeners);
						for (RuntimeServiceListener listener : arrayList) {
							if (listener.support(event)) {
								listener.handleEvent(event);
								if (log.isDebugEnabled()) {
									log.debug("服务运行时异步监听器[" + listener.getClass() + "]处理事件!");
								}
							}
						}
					}
				} catch (Exception e) {
					if (e instanceof InterruptedException) {
						// ignore
						return;
					}
					log.error("执行异步事件" + event + "出现错误", e);
				}
			}
		}
	}

	public void publishEvent(RuntimeServiceEvent event) {
		synchronized (monitor) {
			for (RuntimeServiceListener listener : listeners) {
				if (listener.support(event)) {
					listener.handleEvent(event);
					if (log.isDebugEnabled()) {
						log.debug("事件监听器[" + listener.getClass() + "]处理事件!");
					}
				}
			}
		}
	}

	public void postEvent(RuntimeServiceEvent event) {
		eventQueue.offer(event);
	}

	public void removeServiceListener(RuntimeServiceListener serviceListener) {
		synchronized (monitor) {
			this.listeners.remove(serviceListener);
		}
	}

	public void addServiceListener(RuntimeServiceListener serviceListener) {
		synchronized (monitor) {
			this.listeners.add(serviceListener);
		}

		if (log.isInfoEnabled()) {
			log.info("注册监听器- [class=" + serviceListener.getClass() + "]");
		}
	}

}
