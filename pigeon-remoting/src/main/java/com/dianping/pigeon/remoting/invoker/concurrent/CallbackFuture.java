/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.concurrent;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.dianping.avatar.tracker.TrackerContext;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.UnifiedResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.invoker.process.ExceptionManager;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;
import com.dianping.pigeon.util.ContextUtils;

public class CallbackFuture implements Callback, CallFuture {

    private static final Logger logger = LoggerLoader.getLogger(CallbackFuture.class);
    protected static final Monitor monitor = MonitorLoader.getMonitor();

    protected InvocationResponse response;
    private CallFuture future;
    private boolean done = false;
    private boolean concelled = false;
    private boolean success = false;
    protected InvocationRequest request;
    protected Client client;
    protected MonitorTransaction transaction;

    public CallbackFuture() {
        transaction = monitor.getCurrentCallTransaction();
    }

    public void run() {
        synchronized (this) {
            this.done = true;
            if (this.response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
                this.success = true;
            }
            this.notifyAll();
        }
    }

    public void callback(InvocationResponse response) {
        this.response = response;
    }

    public InvocationResponse getResponse() throws InterruptedException {
        return getResponse(Long.MAX_VALUE);
    }

    protected InvocationResponse waitResponse(long timeoutMillis) throws InterruptedException {
        if (response != null && response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
            return this.response;
        }
        if (request == null && response != null) {
            return response;
        }
        synchronized (this) {
            long start = request.getCreateMillisTime();
            while (!this.done) {
                long timeoutMillis_ = timeoutMillis - (System.currentTimeMillis() - start);
                if (timeoutMillis_ <= 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("request timeout, current time:").append(System.currentTimeMillis())
                            .append("\r\nrequest:").append(request);
                    ServiceStatisticsHolder.flowOut(request, client.getAddress());
                    RequestTimeoutException e = new RequestTimeoutException(sb.toString());
                    throw e;
                } else {
                    this.wait(timeoutMillis_);
                }
            }
            return this.response;
        }
    }

    public InvocationResponse getResponse(long timeoutMillis) throws InterruptedException {
        waitResponse(timeoutMillis);
        processContext();

        if (response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
            ExceptionManager.INSTANCE.logRemoteCallException(client.getAddress(), request.getServiceName(),
                    request.getMethodName(), "remote call error", request, response, transaction);
        } else if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
            ExceptionManager.INSTANCE.logRemoteServiceException("remote service biz error", request, response);
        }

        return this.response;
    }

    protected void processContext() {
        if (response instanceof UnifiedResponse) {
            processContext0((UnifiedResponse) response);
        } else {
            processContext0();
        }
    }

    protected void processContext0() {
        setResponseContext(response);
        Object context = ContextUtils.getContext();
        if (context != null) {
            Integer order = ContextUtils.getOrder(this.response.getContext());
            if (order != null && order > 0) {
                ContextUtils.setOrder(context, order);
            }
            if (this.success) {
                // 传递业务上下文
                ContextUtils.addSuccessContext(this.response.getContext());
            } else {
                // 传递业务上下文
                ContextUtils.addFailedContext(this.response.getContext());
            }
            TrackerContext currentContext = (TrackerContext) context;
            TrackerContext responseContext = (TrackerContext) response.getContext();
            if (responseContext != null && responseContext.getExtension() != null) {
                if (currentContext.getExtension() == null)
                    currentContext.setExtension(responseContext.getExtension());
                else
                    currentContext.getExtension().putAll(responseContext.getExtension());
            }
        }
    }

    protected void setResponseContext(InvocationResponse response) {
        if (response == null) {
            return;
        }

        if (response instanceof UnifiedResponse) {
            UnifiedResponse _response = (UnifiedResponse) response;
            Map<String, String> responseValues = _response.getLocalContext();
            if (responseValues != null) {
                ContextUtils.setResponseContext((Map) responseValues);
            }
        }else{
            Map<String, Serializable> responseValues = response.getResponseValues();
            if (responseValues != null) {
                ContextUtils.setResponseContext(responseValues);
            }
        }
    }


    protected void processContext0(UnifiedResponse response) {
        if (response != null) {
            UnifiedResponse _response = (UnifiedResponse) response;
            Map<String, String> responseValues = _response.getLocalContext();
            if (responseValues != null) {
                ContextUtils.setResponseContext((Map) responseValues);
            }
        }
    }

    public InvocationResponse getResponse(long timeout, TimeUnit unit) throws InterruptedException {
        return getResponse(unit.toMillis(timeout));
    }

    public boolean cancel() {
        if (this.future != null) {
            synchronized (this) {
                this.concelled = this.future.cancel();
                this.notifyAll();
            }
        }
        return this.concelled;
    }

    public boolean isCancelled() {
        return this.concelled;
    }

    public boolean isDone() {
        return this.done;
    }

    public void setRequest(InvocationRequest request) {
        this.request = request;
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public Client getClient() {
        return this.client;
    }

    @Override
    public void dispose() {

    }

}
