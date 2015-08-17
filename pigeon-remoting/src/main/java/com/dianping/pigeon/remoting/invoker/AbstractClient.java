package com.dianping.pigeon.remoting.invoker;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.invoker.callback.Callback;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessorFactory;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;

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

	public InvocationResponse write(InvocationRequest request) throws NetworkException {
		return write(request, null);
	}

	public InvocationResponse write(InvocationRequest request, Callback callback) throws NetworkException {
		ServiceStatisticsHolder.flowIn(request, this.getAddress());
		try {
			return doWrite(request, callback);
		} catch (NetworkException e) {
			ServiceStatisticsHolder.flowOut(request, this.getAddress());
			throw e;
		}
	}

	public abstract InvocationResponse doWrite(InvocationRequest request, Callback callback) throws NetworkException;

}
