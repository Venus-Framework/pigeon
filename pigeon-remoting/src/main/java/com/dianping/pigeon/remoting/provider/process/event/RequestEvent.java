/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.event;

import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.lmax.disruptor.EventFactory;

public class RequestEvent {

	private ProviderContext providerContext;

	public ProviderContext getProviderContext() {
		return providerContext;
	}

	public void setProviderContext(ProviderContext providerContext) {
		this.providerContext = providerContext;
	}

	public final static EventFactory<RequestEvent> EVENT_FACTORY = new EventFactory<RequestEvent>() {
		public RequestEvent newInstance() {
			return new RequestEvent();
		}
	};
}
