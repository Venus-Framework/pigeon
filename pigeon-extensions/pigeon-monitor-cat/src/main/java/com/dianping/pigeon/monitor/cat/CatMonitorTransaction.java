/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.monitor.cat;

import java.util.HashMap;
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
	private Map<String, Object> dataMap = new HashMap<String, Object>();
	String rootMessageId = null;
	String parentMessageId = null;
	String currentMessageId = null;

	public CatMonitorTransaction(CatMonitor monitor, Transaction transaction, InvocationContext invocationContext) {
		this.monitor = monitor;
		this.transaction = transaction;
		this.invocationContext = invocationContext;
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
		this.transaction.setStatus(t);
	}

	public void setDuration(long duration) {
		((DefaultTransaction) this.transaction).setDurationInMillis(duration);
	}

	public long getDuration() {
		return ((DefaultTransaction) this.transaction).getDurationInMillis();
	}

	public void setDurationStart(long durationStart) {
		((DefaultTransaction) this.transaction).setDurationStart(durationStart);
	}
	
	public long getDurationStart() {
		return ((DefaultTransaction) this.transaction).getTimestamp();
	}

	@Override
	public void complete() {
		this.transaction.complete();
	}

	@Override
	public void setStatusOk() {
		this.transaction.setStatus(Transaction.SUCCESS);
	}

	@Override
	public void addData(String name, Object data) {
		this.transaction.addData(name, data);
		dataMap.put(name, data);
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

				setCurrentMessageId(currentMessageId);
				setParentMessageId(serverMessageId);
				setRootMessageId(rootMessageId);
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

	public String getCurrentMessageId() {
		return currentMessageId;
	}

	public void setCurrentMessageId(String currentMessageId) {
		this.currentMessageId = currentMessageId;
	}

	@Override
	public Map<String, Object> getDataMap() {
		return dataMap;
	}

}
