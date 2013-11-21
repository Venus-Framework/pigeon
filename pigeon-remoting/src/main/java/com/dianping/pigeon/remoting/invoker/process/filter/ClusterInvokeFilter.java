/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.filter;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.component.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.component.context.InvokerContext;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: ClusterInvokeFilter.java, v 0.1 2013-6-18 下午9:50:27
 *          jianhuihuang Exp $
 */
public abstract class ClusterInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(ClusterInvokeFilter.class);

	protected static ClientManager clientManager = ClientManager.getInstance();

	private static AtomicLong requestSequenceMaker = new AtomicLong();
	public static final String CONTEXT_CLUSTER_ITEM = "context-cluster-item";

	public abstract String name();

	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		try {
			return _invoke(handler, invocationContext);
		} catch (Exception e) {
			logger.error("Invoke remote call failed.", e);
			throw e;
		}
	}

	public abstract InvocationResponse _invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable;

	protected DefaultRequest createRemoteCallRequest(InvokerContext invocationContext, InvokerConfig invokerConfig) {
		DefaultRequest request = new DefaultRequest(invokerConfig.getUrl(), invocationContext.getMethod().getName(),
				invocationContext.getArguments(), invokerConfig.getSerialize(), Constants.MESSAGE_TYPE_SERVICE,
				invokerConfig.getTimeout(), invocationContext.getMethod().getParameterTypes());
		request.setVersion(invokerConfig.getVersion());
		request.setSequence(requestSequenceMaker.incrementAndGet() * -1); // (*
																			// -1):
																			// in
																			// order
																			// to
																			// distinguish
																			// from
																			// old
																			// logic
		request.setAttachment(Constants.REQ_ATTACH_WRITE_BUFF_LIMIT, invokerConfig.isWriteBufferLimit());
		if (Constants.CALL_ONEWAY.equalsIgnoreCase(invokerConfig.getCallMethod())) {
			request.setCallType(Constants.CALLTYPE_NOREPLY);
		} else {
			request.setCallType(Constants.CALLTYPE_REPLY);
		}
		invocationContext.setRequest(request);
		return request;
	}

}
