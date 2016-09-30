/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker;

import com.dianping.pigeon.remoting.common.channel.Channel;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.route.region.Region;

import java.util.List;


public interface Client<C extends Channel> {

    ConnectInfo getConnectInfo();

    void open();

    void close();

    InvocationResponse write(InvocationRequest request) throws NetworkException;

    void processResponse(InvocationResponse response);

    void setActive(boolean active);

    boolean isActive();

    boolean isClosed();

    List<C> getChannels();

    String getHost();

    String getAddress();

    int getPort();

    String getProtocol();

    Region getRegion();

    void clearRegion();

}
