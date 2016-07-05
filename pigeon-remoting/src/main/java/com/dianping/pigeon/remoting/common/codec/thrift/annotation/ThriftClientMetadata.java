package com.dianping.pigeon.remoting.common.codec.thrift.annotation;

import com.facebook.swift.codec.ThriftCodecManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;
import java.util.Map;

/**
 * @author qi.yin
 *         2016/05/24  上午10:41.
 */
@Immutable
public class ThriftClientMetadata {
    private final String clientType;
    private final String clientName;
    private final ThriftServiceMetadata thriftServiceMetadata;
    private final Map<String, ThriftMethodHandler> methodHandlers;

    private final ThriftCodecManager codecManager = new ThriftCodecManager();

    public ThriftClientMetadata(
            Class<?> clientType,
            String clientName) {
        Preconditions.checkNotNull(clientType, "clientType is null");
        Preconditions.checkNotNull(clientName, "clientName is null");
        Preconditions.checkNotNull(codecManager, "codecManager is null");

        this.clientName = clientName;
        thriftServiceMetadata = new ThriftServiceMetadata(clientType, codecManager.getCatalog());
        this.clientType = thriftServiceMetadata.getName();
        ImmutableMap.Builder<String, ThriftMethodHandler> methods = ImmutableMap.builder();
        for (ThriftMethodMetadata methodMetadata : thriftServiceMetadata.getMethods().values()) {
            ThriftMethodHandler methodHandler = new ThriftMethodHandler(methodMetadata, codecManager);
            methods.put(methodMetadata.getMethod().getName(), methodHandler);
        }
        methodHandlers = methods.build();
    }

    public String getClientType() {
        return clientType;
    }

    public String getClientName() {
        return clientName;
    }

    public String getName() {
        return thriftServiceMetadata.getName();
    }

    public Map<String, ThriftMethodHandler> getMethodHandlers() {
        return methodHandlers;
    }

    public ThriftMethodHandler getMethodHandler(String method) {
        return methodHandlers.get(method);
    }
}
