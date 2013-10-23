/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.component.context;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.remoting.common.component.context.AbstractInvocationContext;
import com.dianping.pigeon.remoting.provider.component.ProviderChannel;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: InvocationProcessContextImpl.java, v 0.1 2013-6-30 下午7:41:22
 *          jianhuihuang Exp $
 */
public class DefaultProviderContext extends AbstractInvocationContext implements ProviderContext {

	private Throwable serviceError;
	private ProviderChannel channel;

	public DefaultProviderContext(InvocationRequest request, ProviderChannel channel) {
		super(request);
		this.channel = channel;
	}

	public Throwable getServiceError() {
		return serviceError;
	}

	public void setServiceError(Throwable serviceError) {
		this.serviceError = serviceError;
	}

	@Override
	public ProviderChannel getChannel() {
		return channel;
	}

}
