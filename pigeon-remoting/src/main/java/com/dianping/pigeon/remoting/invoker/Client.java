/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.component.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.component.async.CallFuture;
import com.dianping.pigeon.remoting.invoker.component.async.Callback;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: Client.java, v 0.1 2013-7-16 上午10:36:30 jianhuihuang Exp $
 */
public interface Client {

	ConnectInfo getConnectInfo();

	String getServiceName();

	void connect();

	CallFuture write(InvocationRequest message, Callback callback);

	void write(InvocationRequest message);

	void connectionException(Object attachment, Throwable e);

	void processResponse(InvocationResponse response);

	boolean isConnected();

	boolean isActive();

	void setActive(boolean active);

	void setActiveSetable(boolean activesetable);

	boolean isWritable();

	String getHost();

	String getAddress();

	int getPort();

	void close();

}
