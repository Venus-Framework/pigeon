/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.monitor.cat;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.phoenix.environment.PhoenixContext;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.util.ContextUtils;

/**
 * @author xiangwu
 * @Sep 25, 2013
 * 
 */
public class CatMonitorTransaction implements MonitorTransaction {

	private MonitorLogger logger = null;
	private Transaction transaction = null;
	private InvocationContext invocationContext = null;

	public static final String REQUEST_ID = "requestId";
	public static final String REFER_REQUEST_ID = "referRequestId";

	public CatMonitorTransaction(CatLogger logger, Transaction transaction, InvocationContext invocationContext) {
		this.logger = logger;
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
		this.transaction.setStatus(t);
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
	}

	public void setInvocationContext(InvocationContext invocationContext) {
		this.invocationContext = invocationContext;
	}

	public InvocationContext getInvocationContext() {
		return invocationContext;
	}

	@Override
	public MonitorLogger getLogger() {
		return logger;
	}

	@Override
	public void readMonitorContext() {
		InvocationContext invocationContext = getInvocationContext();
		if (invocationContext != null) {
			CatLogger logger = (CatLogger) getLogger();
			MessageProducer producer = logger.getMessageProducer();
			String serverMessageId = logger.getMessageProducer().createMessageId();
			DefaultMessageManager messageManager = (DefaultMessageManager) Cat.getManager();
			String metricType = messageManager.getMetricType();
			MessageTree tree = messageManager.getThreadLocalMessageTree();
			if (tree == null) {
				Cat.setup(null);
				tree = Cat.getManager().getThreadLocalMessageTree();
			}
			String rootMessageId = tree.getRootMessageId() == null ? tree.getMessageId() : tree.getRootMessageId();
			String currentMessageId = tree.getMessageId();

			invocationContext.putContextValue(CatConstants.PIGEON_ROOT_MESSAGE_ID, rootMessageId);
			invocationContext.putContextValue(CatConstants.PIGEON_CURRENT_MESSAGE_ID, currentMessageId);
			invocationContext.putContextValue(CatConstants.PIGEON_SERVER_MESSAGE_ID, serverMessageId);
			invocationContext.putContextValue("metricType", metricType);
			// Transfer requestId and referRequestId
			invocationContext.putContextValue(REQUEST_ID, PhoenixContext.getInstance().getRequestId());
			invocationContext.putContextValue(REFER_REQUEST_ID, PhoenixContext.getInstance().getReferRequestId());

			producer.logEvent(CatConstants.TYPE_REMOTE_CALL, CatConstants.NAME_REQUEST, Transaction.SUCCESS,
					serverMessageId);
		}

	}

	@Override
	public void writeMonitorContext() {
		InvocationContext invocationContext = getInvocationContext();
		if (invocationContext != null) {
			InvocationRequest request = invocationContext.getRequest();
			Object context = request.getContext();
			String rootMessageId = ContextUtils.getContextValue(context, CatConstants.PIGEON_ROOT_MESSAGE_ID);
			String serverMessageId = ContextUtils.getContextValue(context, CatConstants.PIGEON_CURRENT_MESSAGE_ID);
			String currentMessageId = ContextUtils.getContextValue(context, CatConstants.PIGEON_SERVER_MESSAGE_ID);
			String metricType = ContextUtils.getContextValue(context, "metricType");
			// Set requestId & referRequestId
			String requestId = ContextUtils.getContextValue(context, REQUEST_ID);
			String referRequestId = ContextUtils.getContextValue(context, REFER_REQUEST_ID);
			PhoenixContext.getInstance().setRequestId(requestId);
			PhoenixContext.getInstance().setReferRequestId(referRequestId);

			DefaultMessageManager messageManager = (DefaultMessageManager) Cat.getManager();
			MessageTree tree = messageManager.getThreadLocalMessageTree();
			if (tree == null) {
				Cat.setup(null);
				tree = Cat.getManager().getThreadLocalMessageTree();
			}
			messageManager.setMetricType(metricType);
			tree.setRootMessageId(rootMessageId);
			tree.setParentMessageId(serverMessageId);
			tree.setMessageId(currentMessageId);
		}
	}

}
