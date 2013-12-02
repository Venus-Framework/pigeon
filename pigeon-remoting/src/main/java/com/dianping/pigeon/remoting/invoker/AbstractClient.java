package com.dianping.pigeon.remoting.invoker;

import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessorFactory;

public abstract class AbstractClient implements Client {

	ResponseProcessor responseProcessor = ResponseProcessorFactory.selectProcessor();

	@Override
	public void connectionException(Object attachment, Throwable e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processResponse(InvocationResponse response) {
		this.responseProcessor.processResponse(response, this);
	}

}
