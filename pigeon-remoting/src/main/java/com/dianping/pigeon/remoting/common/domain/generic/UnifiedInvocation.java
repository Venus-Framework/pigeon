package com.dianping.pigeon.remoting.common.domain.generic;

import com.dianping.pigeon.remoting.common.domain.InvocationSerializable;

import java.util.Map;

/**
 * @author qi.yin
 *         2016/05/24  下午5:31.
 */
public interface UnifiedInvocation extends InvocationSerializable {

    byte getProtocalVersion();

    void setProtocalVersion(byte protocalVersion);

    int getCompressType();

    void setCompressType(int compressType);


    Map<String, String> getGlobalContext();

    void setGlobalContext(Map<String, String> globalContext);

    Map<String, String> getLocalContext();

    void setLocalContext(Map<String, String> localContext);
}
