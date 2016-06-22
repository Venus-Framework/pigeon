/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.dianping.pigeon.remoting.common.domain.generic.UnifiedRequest;
import com.dianping.pigeon.remoting.common.domain.generic.UnifiedResponse;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.util.ContextUtils;

public class ContextTransferProcessFilter implements ServiceInvocationFilter<ProviderContext> {

    private static final Logger logger = LoggerLoader.getLogger(ContextTransferProcessFilter.class);

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
            throws Throwable {
		invocationContext.getTimeline().add(new TimePoint(TimePhase.C));
        InvocationRequest request = invocationContext.getRequest();
        transferContextValueToProcessor(invocationContext, request);
        InvocationResponse response = null;
        try {
            response = handler.handle(invocationContext);
            return response;
        } finally {
            if (response != null) {
                try {
                    transferContextValueToResponse(invocationContext, response);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private void transferContextValueToProcessor(final ProviderContext processContext, final InvocationRequest request) {
        if (request instanceof UnifiedRequest) {
            UnifiedRequest _request = (UnifiedRequest) request;
            transferContextValueToProcessor0(processContext, _request);
        } else {
            transferContextValueToProcessor0(processContext, request);
        }
    }

    private void transferContextValueToProcessor0(final ProviderContext processContext, final InvocationRequest request) {
        Object contextHolder = request.getContext();
        Map<String, Serializable> ctx = null;
        if (contextHolder != null) {
            ContextUtils.setContext(contextHolder);
            ctx = ContextUtils.getContextValues(contextHolder);
        }
        if (ctx != null) {
            for (Map.Entry<String, Serializable> entry : ctx.entrySet()) {
                processContext.putContextValue(entry.getKey(), entry.getValue());
            }
        }

        ContextUtils.setGlobalContext(request.getGlobalValues());

        ContextUtils.putLocalContext("CLIENT_IP", processContext.getChannel().getRemoteAddress());
        ContextUtils.putLocalContext("CLIENT_APP", request.getApp());
        Map<String, Serializable> requestValues = request.getRequestValues();
        if (requestValues != null) {
            for (String key : requestValues.keySet()) {
                ContextUtils.putLocalContext(key, requestValues.get(key));
            }
        }
    }

    private void transferContextValueToProcessor0(final ProviderContext processContext, final UnifiedRequest request) {
        ContextUtils.setGlobalContext((Map) request.getGlobalContext());

        ContextUtils.putLocalContext("CLIENT_IP", processContext.getChannel().getRemoteAddress());
        ContextUtils.putLocalContext("CLIENT_APP", request.getApp());
        Map<String, String> requestValues = request.getLocalContext();
        if (requestValues != null) {
            for (String key : requestValues.keySet()) {
                ContextUtils.putLocalContext(key, requestValues.get(key));
            }
        }
    }

    private void transferContextValueToResponse(final ProviderContext processContext, final InvocationResponse response) {
        if (response instanceof UnifiedResponse) {
            UnifiedResponse _response = (UnifiedResponse) response;
            transferContextValueToResponse0(processContext, _response);
        } else {
            transferContextValueToResponse0(processContext, response);
        }
    }

    private void transferContextValueToResponse0(final ProviderContext processContext, final InvocationResponse response) {
        Object contextHolder = ContextUtils.getContext();
        Map<String, Serializable> contextValues = processContext.getContextValues();
        if (contextHolder != null) {
            if (contextValues != null) {
                for (Map.Entry<String, Serializable> entry : contextValues.entrySet()) {
                    ContextUtils.putContextValue(contextHolder, entry.getKey(), entry.getValue());
                }
            }
            response.setContext(contextHolder);
        }
        response.setResponseValues(ContextUtils.getResponseContext());
    }

    private void transferContextValueToResponse0(final ProviderContext processContext, final UnifiedResponse response) {
        Map<String, String> _localContext = response.getLocalContext();

        if (_localContext == null) {
            _localContext = new HashMap<String, String>();
            response.setLocalContext(_localContext);
        }

        ContextUtils.convertContext(ContextUtils.getResponseContext(), _localContext);
        response.setLocalContext(_localContext);
    }

}
