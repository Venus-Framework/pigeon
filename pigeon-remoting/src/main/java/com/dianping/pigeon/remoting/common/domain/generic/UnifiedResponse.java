package com.dianping.pigeon.remoting.common.domain.generic;

import com.dianping.pigeon.remoting.common.domain.InvocationResponse;

/**
 * @author qi.yin
 *         2016/05/24  下午5:25.
 */
public interface UnifiedResponse extends InvocationResponse, UnifiedInvocation {

    String getServiceName();

    void setServiceName(String serviceName);

    String getMethodName();

    void setMethodName(String methodName);

    boolean hasException();

    void setPort(int port);

    int getPort();
}
