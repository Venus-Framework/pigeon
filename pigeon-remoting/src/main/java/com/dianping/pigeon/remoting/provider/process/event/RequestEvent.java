/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.event;

import com.dianping.pigeon.remoting.provider.domain.ProviderContext;

public class RequestEvent {

	private ProviderContext providerContext;

	public ProviderContext getProviderContext() {
		return providerContext;
	}

	public void setProviderContext(ProviderContext providerContext) {
		this.providerContext = providerContext;
	}

}
