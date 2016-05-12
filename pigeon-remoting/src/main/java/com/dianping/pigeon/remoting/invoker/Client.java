/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.invoker.callback.Callback;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.route.region.Region;

public interface Client {

	ConnectInfo getConnectInfo();

	void connect();

	InvocationResponse write(InvocationRequest request, Callback callback) throws NetworkException;

	InvocationResponse write(InvocationRequest request) throws NetworkException;

	void connectionException(Object attachment, Throwable e);

	void processResponse(InvocationResponse response);

	boolean isConnected();

	boolean isActive();

	void setActive(boolean active);

	boolean isWritable();

	String getHost();

	String getAddress();

	int getPort();

	void close();

	boolean isDisposable();

	void dispose();
	
	String getProtocol();

	Region getRegion();

	void clearRegion();

}
