/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.monitor.cat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.util.ContextUtils;

/**
 * @author xiangwu
 * @Sep 25, 2013
 * 
 */
public class CatMonitorTransaction implements MonitorTransaction {

	private CatMonitor monitor = null;
	private Transaction transaction = null;
	private InvocationContext invocationContext = null;
	private String name;
	private String uri;
	private Map<String, Object> dataMap = null;
	String rootMessageId = null;
	String parentMessageId = null;
	String invokerMessageId = null;
	String providerMessageId = null;
	String serverMessageId = null;
	boolean autoCommit = true;
	private List<CatEvent> events = null;
	private long startTime;
	private List<MonitorTransaction> transactions = null;

	public CatMonitorTransaction(CatMonitor monitor, Transaction transaction, InvocationContext invocationContext) {
		this.monitor = monitor;
		this.transaction = transaction;
		this.invocationContext = invocationContext;
		this.startTime = System.nanoTime();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	@Override
	public void setStatusError(Throwable t) {
		if (autoCommit && this.transaction != null) {
			this.transaction.setStatus(t);
		}
	}

	public void setStartTime(long startTime) {
		if (autoCommit && this.transaction != null) {
			((DefaultTransaction) this.transaction).setDurationStart(startTime);
		} else {
			this.startTime = startTime;
		}
	}

	public long getStartTime() {
		return this.startTime;
	}

	@Override
	public void complete() {
		if (this.transaction != null) {
			this.transaction.complete();
		}
	}

	@Override
	public void setStatusOk() {
		if (autoCommit && this.transaction != null) {
			this.transaction.setStatus(Transaction.SUCCESS);
		}
	}

	@Override
	public void addData(String name, Object data) {
		if (autoCommit && this.transaction != null) {
			this.transaction.addData(name, data);
		} else {
			if (dataMap == null) {
				dataMap = new HashMap<String, Object>();
			}
			dataMap.put(name, data);
		}
	}

	public void setInvocationContext(InvocationContext invocationContext) {
		this.invocationContext = invocationContext;
	}

	public InvocationContext getInvocationContext() {
		return invocationContext;
	}

	@Override
	public void readMonitorContext() {
		InvocationContext invocationContext = getInvocationContext();
		if (invocationContext != null) {
			MessageProducer producer = monitor.getMessageProducer();
			MessageManager messageManager = Cat.getManager();
			MessageTree tree = messageManager.getThreadLocalMessageTree();
			if (tree == null) {
				Cat.setup(null);
				tree = Cat.getManager().getThreadLocalMessageTree();
			}
			String currentMessageId = tree.getMessageId();
			if (currentMessageId == null) {
				currentMessageId = producer.createMessageId();
				tree.setMessageId(currentMessageId);
			}
			String serverMessageId = monitor.getMessageProducer().createMessageId();
			String rootMsgId = tree.getRootMessageId();
			String rootMessageId = rootMsgId == null ? currentMessageId : rootMsgId;

			ContextUtils.putRequestContext(CatConstants.PIGEON_ROOT_MESSAGE_ID, rootMessageId);
			ContextUtils.putRequestContext(CatConstants.PIGEON_CURRENT_MESSAGE_ID, currentMessageId);
			ContextUtils.putRequestContext(CatConstants.PIGEON_SERVER_MESSAGE_ID, serverMessageId);

			ContextUtils.putLocalContext(CatConstants.PIGEON_CURRENT_MESSAGE_ID, currentMessageId);

			setInvokerMessageId(currentMessageId);
			setServerMessageId(serverMessageId);
			setRootMessageId(rootMessageId);

			producer.logEvent(CatConstants.TYPE_REMOTE_CALL, CatConstants.NAME_REQUEST, Transaction.SUCCESS,
					serverMessageId);
		}

	}

	public void writeMonitorContext() {
		InvocationContext invocationContext = getInvocationContext();
		if (invocationContext != null) {
			String rootMessageId = (String) ContextUtils.getLocalContext(CatConstants.PIGEON_ROOT_MESSAGE_ID);
			String serverMessageId = (String) ContextUtils.getLocalContext(CatConstants.PIGEON_CURRENT_MESSAGE_ID);
			String currentMessageId = (String) ContextUtils.getLocalContext(CatConstants.PIGEON_SERVER_MESSAGE_ID);

			setProviderMessageId(currentMessageId);
			setParentMessageId(serverMessageId);
			setRootMessageId(rootMessageId);

			MessageManager messageManager = Cat.getManager();
			MessageTree tree = messageManager.getThreadLocalMessageTree();
			if (tree == null) {
				Cat.setup(null);
				tree = Cat.getManager().getThreadLocalMessageTree();
			}
			if (tree != null) {
				tree.setRootMessageId(rootMessageId);
				tree.setParentMessageId(serverMessageId);
				tree.setMessageId(currentMessageId);
			}
		}
	}

	public String getRootMessageId() {
		return rootMessageId;
	}

	public void setRootMessageId(String rootMessageId) {
		this.rootMessageId = rootMessageId;
	}

	public String getParentMessageId() {
		return parentMessageId;
	}

	public void setParentMessageId(String parentMessageId) {
		this.parentMessageId = parentMessageId;
	}

	public String getInvokerMessageId() {
		return invokerMessageId;
	}

	public void setInvokerMessageId(String invokerMessageId) {
		this.invokerMessageId = invokerMessageId;
	}

	public String getProviderMessageId() {
		return providerMessageId;
	}

	public void setProviderMessageId(String providerMessageId) {
		this.providerMessageId = providerMessageId;
	}

	public String getServerMessageId() {
		return serverMessageId;
	}

	public void setServerMessageId(String serverMessageId) {
		this.serverMessageId = serverMessageId;
	}

	@Override
	public Map<String, Object> getDataMap() {
		return dataMap;
	}

	@Override
	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	@Override
	public boolean isAutoCommit() {
		return autoCommit;
	}

	@Override
	public void logEvent(String name, String event, String desc) {
		if (autoCommit) {
			monitor.logEvent(name, event, desc);
		} else {
			if (events == null) {
				events = new ArrayList<CatEvent>();
			}
			events.add(new CatEvent(name, event, desc));
		}
	}

	public List<CatEvent> getEvents() {
		return events;
	}

	public class CatEvent {
		String name;
		String event;
		String desc;

		public CatEvent(String name, String event, String desc) {
			this.name = name;
			this.event = event;
			this.desc = desc;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getEvent() {
			return event;
		}

		public void setEvent(String event) {
			this.event = event;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

	}

	@Override
	public void addTransaction(MonitorTransaction transaction) {
		if (transactions == null) {
			transactions = new ArrayList<MonitorTransaction>();
		}
		transactions.add(transaction);
	}

	public List<MonitorTransaction> getTransactions() {
		return transactions;
	}

}
