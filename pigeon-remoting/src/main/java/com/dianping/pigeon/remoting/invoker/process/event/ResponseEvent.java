/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.event;

import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.Client;

public class ResponseEvent {

	private InvocationResponse response;
	private Client client;

	public InvocationResponse getResponse() {
		return response;
	}

	public void setResponse(InvocationResponse response) {
		this.response = response;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

}
