/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.monitor.cat;

import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
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

	public CatMonitorTransaction(CatMonitor monitor, Transaction transaction, InvocationContext invocationContext) {
		this.monitor = monitor;
		this.transaction = transaction;
		this.invocationContext = invocationContext;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	@Override
	public void setStatusError(Throwable t) {
		if (this.transaction != null) {
			this.transaction.setStatus(t);
		}
	}

	@Override
	public void complete() {
		this.complete(0);
	}

	@Override
	public void complete(long startTime) {
		if (this.transaction != null) {
			long now = System.currentTimeMillis();
			this.invocationContext.getTimeline().add(new TimePoint(TimePhase.E, now));
			List<TimePoint> timeline = this.invocationContext.getTimeline();
			StringBuilder s = new StringBuilder();
			s.append(timeline.get(0));
			for (int i = 1; i < timeline.size(); i++) {
				TimePoint tp = timeline.get(i);
				TimePoint tp2 = timeline.get(i - 1);
				s.append(",").append(tp.getPhase()).append(tp.getTime() - tp2.getTime());
			}
			long duration = 0;
			long start = startTime;
			if (startTime <= 0) {
				start = timeline.get(0).getTime();
			}
			duration = now - start;
			if (this.transaction instanceof DefaultTransaction) {
				((DefaultTransaction) this.transaction).setDurationStart(start * 1000 * 1000);
			}
			this.transaction.addData("Timeline", s.toString());
			this.transaction.complete();
			if (this.transaction instanceof DefaultTransaction) {
				((DefaultTransaction) this.transaction).setDurationInMillis(duration);
			}
		}
	}

	@Override
	public void setStatusOk() {
		if (this.transaction != null) {
			this.transaction.setStatus(Transaction.SUCCESS);
		}
	}

	@Override
	public void addData(String name, Object data) {
		if (this.transaction != null) {
			this.transaction.addData(name, data);
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
			}
		}
	}

	@Override
	public void logEvent(String name, String event, String desc) {
		monitor.logEvent(name, event, desc);
	}

}
