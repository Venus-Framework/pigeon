/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
/**
 *
 */
package com.dianping.pigeon.remoting.netty.invoker;

import java.util.Map;

import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientFactory;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.util.CollectionUtils;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessorFactory;

/**
 *
 */
public class NettyClientFactory implements ClientFactory {

    private final static ResponseProcessor responseProcessor =
            ResponseProcessorFactory.selectProcessor();

    @Override
    public boolean support(ConnectInfo connectInfo) {
        Map<String, Integer> serviceNames = connectInfo.getServiceNames();
        if (!CollectionUtils.isEmpty(serviceNames)) {
            String name = serviceNames.keySet().iterator().next();
            if (name.startsWith("@")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Client createClient(ConnectInfo connectInfo) {
        return new NettyClient(
                connectInfo,
                Constants.CONNECT_TIMEOUT,
                Constants.WRITE_BUFFER_HIGH_WATER,
                Constants.WRITE_BUFFER_LOW_WATER,
                Constants.getChannelPoolInitialSize(),
                Constants.getChannelPoolNormalSize(),
                Constants.getChannelPoolMaxActive(),
                Constants.getChannelPoolMaxWait(),
                Constants.getChannelPoolTimeBetweenCheckerMillis(),
                responseProcessor,
                Constants.getInvokerHeartbeatEnable(),
                Constants.getInvokerHeartbeatTimeout(),
                Constants.getDefaultInvokerClientDeadthreshold(),
                Constants.getInvokerHeartbeatInterval()
        );
    }

}
