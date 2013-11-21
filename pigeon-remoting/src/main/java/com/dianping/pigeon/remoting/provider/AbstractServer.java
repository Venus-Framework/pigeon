package com.dianping.pigeon.remoting.provider;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;

public abstract class AbstractServer implements Server {

	RequestProcessor requestProcessor = new RequestProcessor();

	@Override
	public void processRequest(InvocationRequest request, ProviderContext providerContext) {
		requestProcessor.processRequest(request, providerContext);
	}

}
