package com.dianping.pigeon.remoting.common.domain.generic;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;


/**
 * @author qi.yin
 *         2016/05/24  下午4:58.
 */
public interface UnifiedRequest extends InvocationRequest, UnifiedInvocation {

    Class<?>[] getParameterTypes();

    void setParameterTypes(Class<?>[] parameterTypes);

    Class<?> getServiceInterface();

    void setServiceInterface(Class<?> serviceInterface);
}
