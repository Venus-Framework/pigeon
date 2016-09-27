package com.dianping.pigeon.remoting.invoker.concurrent;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.proxy.MockProxyWrapper;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

/**
 * Created by chenchongze on 16/9/26.
 */
public class MockCallbackFuture extends ServiceFutureImpl {

    private static final Logger logger = LoggerLoader.getLogger(MockCallbackFuture.class);
    private final MockProxyWrapper mockProxyWrapper;

    public MockCallbackFuture(MockProxyWrapper mockProxyWrapper, InvokerContext invokerContext, long timeout) {
        super(invokerContext, timeout);
        this.mockProxyWrapper = mockProxyWrapper;
    }

    @Override
    public Object get(long timeoutMillis) throws InterruptedException, ExecutionException {
        // 此刻执行mock逻辑或脚本
        try {
            return mockProxyWrapper.invoke(
                    invocationContext.getMethodName(),
                    invocationContext.getParameterTypes(),
                    invocationContext.getArguments());
        } catch (Throwable t) {
            if(!(t instanceof RuntimeException)) {
                t = new RuntimeException(t);
            }
            throw (RuntimeException) t;
        }
    }
}
