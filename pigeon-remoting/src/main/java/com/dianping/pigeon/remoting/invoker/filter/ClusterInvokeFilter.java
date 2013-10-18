/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.filter;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.dianping.dpsf.component.DPSFResponse;
import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.component.InvokerMetaData;
import com.dianping.pigeon.remoting.invoker.component.context.InvokerContext;
import com.dianping.pigeon.remoting.invoker.service.ClientManager;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: ClusterInvokeFilter.java, v 0.1 2013-6-18 下午9:50:27
 *          jianhuihuang Exp $
 */
public abstract class ClusterInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = Logger.getLogger(ClusterInvokeFilter.class);

	protected static ClientManager clientManager = ClientManager.getInstance();

	private static AtomicLong requestSequenceMaker = new AtomicLong();
	public static final String CONTEXT_CLUSTER_ITEM = "context-cluster-item";

	public abstract String name();

	public DPSFResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {

		try {

			return _invoke(handler, invocationContext);
		} catch (Exception e) {
			logger.error("Invoke remote call failed.", e);
			throw e;
		}
	}

	public abstract DPSFResponse _invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable;

	protected DefaultRequest createRemoteCallRequest(InvokerContext invocationContext, InvokerMetaData metaData) {
		DefaultRequest request = new DefaultRequest(metaData.getServiceName(), invocationContext.getMethod().getName(),
				invocationContext.getArguments(), metaData.getSerialize(), Constants.MESSAGE_TYPE_SERVICE,
				metaData.getTimeout(), invocationContext.getMethod().getParameterTypes());
		request.setSequence(requestSequenceMaker.incrementAndGet() * -1); // (*
																			// -1):
																			// in
																			// order
																			// to
																			// distinguish
																			// from
																			// old
																			// logic
		request.setAttachment(Constants.REQ_ATTACH_WRITE_BUFF_LIMIT, metaData.isWriteBufferLimit());
		if (Constants.CALL_ONEWAY.equalsIgnoreCase(metaData.getCallMethod())) {
			request.setCallType(Constants.CALLTYPE_NOREPLY);
		} else {
			request.setCallType(Constants.CALLTYPE_REPLY);
		}
		invocationContext.setRequest(request);
		return request;
	}

}
