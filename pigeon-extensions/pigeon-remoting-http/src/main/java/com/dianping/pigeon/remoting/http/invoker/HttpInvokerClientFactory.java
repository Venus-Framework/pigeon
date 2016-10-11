/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.http.invoker;

import java.util.Map;

import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.http.HttpUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientFactory;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessorFactory;

public class HttpInvokerClientFactory implements ClientFactory {

    private final static ResponseProcessor responseProcessor =
            ResponseProcessorFactory.selectProcessor();

    @Override
    public Client createClient(ConnectInfo connectInfo) {
        return new HttpInvokerClient(connectInfo,
                responseProcessor,
                Constants.getInvokerHeartbeatEnable(),
                Constants.getInvokerHeartbeatTimeout(),
                Constants.getDefaultInvokerClientDeadthreshold(),
                Constants.getInvokerHeartbeatInterval());
    }

    @Override
    public boolean support(ConnectInfo connectInfo) {
        Map<String, Integer> serviceNames = connectInfo.getServiceNames();
        if (serviceNames != null && !serviceNames.isEmpty()) {
            String name = serviceNames.keySet().iterator().next();
            if (name.startsWith(HttpUtils.URL_PREFIX)) {
                return true;
            }
        }
        return false;
    }
}
