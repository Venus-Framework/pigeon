/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.component;

import java.net.InetSocketAddress;

import com.dianping.dpsf.component.DPSFResponse;

public interface ProviderChannel {

	<C> C getChannel(C c);

	void write(DPSFResponse response);

	InetSocketAddress getRemoteAddress();

}
