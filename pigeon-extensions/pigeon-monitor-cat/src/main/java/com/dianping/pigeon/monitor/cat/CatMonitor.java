/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.monitor.cat;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationContext;

/**
 * @author xiangwu
 * @Sep 25, 2013
 * 
 */
public class CatMonitor implements Monitor {

	private static final Logger logger = LoggerLoader.getLogger(CatMonitor.class);
	private volatile long errorCounter = 0L;
	private MessageProducer producer = null;
	private ThreadLocal<MonitorTransaction> tlTransaction = new ThreadLocal<MonitorTransaction>();

	volatile boolean isInitialized = false;

	@Override
	public void init() {
		if (!isInitialized) {
			try {
				this.producer = Cat.getProducer();
			} catch (Throwable e2) {
				logMonitorError(e2);
			}
			Transaction t = Cat.newTransaction("System", "PigeonClientStart");
			t.setStatus("0");
			t.complete();
			isInitialized = true;
		}
	}

	public MessageProducer getMessageProducer() {
		return producer;
	}

	@Override
	public MonitorTransaction createTransaction(String name, String uri, Object invocationContext) {
		MonitorTransaction transaction = doCreateTransaction(name, uri, invocationContext);
		tlTransaction.set(transaction);
		return transaction;
	}

	private MonitorTransaction doCreateTransaction(String name, String uri, Object invocationContext) {
		if (producer != null) {
			Transaction transaction = producer.newTransaction(name, uri);
			CatMonitorTransaction catTransaction = new CatMonitorTransaction(this, transaction,
					(InvocationContext) invocationContext);
			catTransaction.setName(name);
			catTransaction.setUri(uri);
			tlTransaction.set(catTransaction);
			return catTransaction;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.pigeon.monitor.MonitorLogger#logError(java.lang.Throwable)
	 */
	@Override
	public void logError(Throwable t) {
		if (t != null) {
			try {
				if (producer == null) {
					producer = Cat.getProducer();
				}
				if (producer != null && t != null) {
					producer.logError(t);
				}
			} catch (Throwable e) {
				logMonitorError(e);
			}
		}
	}

	public void logError(String msg, Throwable t) {
		if (t != null) {
			try {
				if (producer == null) {
					producer = Cat.getProducer();
				}
				if (producer != null && t != null) {
					producer.logError(msg, t);
				}
			} catch (Throwable e) {
				logMonitorError(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.pigeon.monitor.MonitorLogger#logEvent(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void logEvent(String name, String event, String desc) {
		try {
			if (producer == null) {
				producer = Cat.getProducer();
			}
			if (producer != null) {
				producer.logEvent(name, event, Event.SUCCESS, desc);
			}
		} catch (Throwable e) {
			logMonitorError(e);
		}
	}

	@Override
	public void logMonitorError(Throwable t) {
		try {
			String errorMsg = "[Cat]Monitor pigeon call failed.";
			if (errorCounter <= 50) {
				logger.error(errorMsg, t);
			} else if (errorCounter < 1000 && errorCounter % 40 == 0) {
				logger.error(errorMsg, t);
			} else if (errorCounter % 200 == 0) {
				logger.error(errorMsg, t);
			}
		} catch (Throwable e2) {/* do nothing */
		}
		errorCounter++;
	}

	@Override
	public MonitorTransaction getCurrentTransaction() {
		return tlTransaction.get();
	}

	public String toString() {
		return "CatMonitor";
	}

	@Override
	public MonitorTransaction copyTransaction(String name, String uri, Object invocationContext,
			MonitorTransaction transaction) {
		CatMonitorTransaction newTransaction = (CatMonitorTransaction) doCreateTransaction(name, uri, invocationContext);
		newTransaction.setDurationStart(((CatMonitorTransaction) transaction).getDurationStart());
		Map<String, Object> dataMap = transaction.getDataMap();
		for (String key : dataMap.keySet()) {
			newTransaction.addData(key, dataMap.get(key));
		}
		Transaction catTransaction = ((CatMonitorTransaction) transaction).getTransaction();
		List<Message> children = catTransaction.getChildren();
		for (Message child : children) {
			newTransaction.getTransaction().addChild(child);
		}
		MessageManager messageManager = Cat.getManager();
		MessageTree tree = messageManager.getThreadLocalMessageTree();
		if (tree == null) {
			Cat.setup(null);
			tree = Cat.getManager().getThreadLocalMessageTree();
		}
		if (tree != null) {
			tree.setRootMessageId(((CatMonitorTransaction) transaction).getRootMessageId());
			tree.setParentMessageId(((CatMonitorTransaction) transaction).getParentMessageId());
			tree.setMessageId(((CatMonitorTransaction) transaction).getCurrentMessageId());
		}

		return newTransaction;
	}

	@Override
	public void clearTransaction() {
		tlTransaction.remove();
	}
}
