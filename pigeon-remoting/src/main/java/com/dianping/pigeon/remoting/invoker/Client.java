/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.invoker.concurrent.Callback;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.route.region.Region;

public interface Client {

    ConnectInfo getConnectInfo();

    void open();

    void close();

    InvocationResponse write(InvocationRequest request) throws NetworkException;

    void processResponse(InvocationResponse response);

    boolean isActive();

    boolean isClosed();

    String getHost();

    String getAddress();

    int getPort();

    String getProtocol();

    Region getRegion();

    void clearRegion();

}
