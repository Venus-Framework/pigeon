/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.domain;

import java.net.InetSocketAddress;

import com.dianping.pigeon.remoting.common.domain.InvocationResponse;

public interface ProviderChannel {

	<C> C getChannel(C c);

	void write(InvocationResponse response);

	InetSocketAddress getRemoteAddress();

}
