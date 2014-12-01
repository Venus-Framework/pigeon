/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.domain;

import com.dianping.pigeon.remoting.common.domain.InvocationResponse;

public interface ProviderChannel {

	void write(InvocationResponse response);

	String getRemoteAddress();

	String getProtocol();
}
