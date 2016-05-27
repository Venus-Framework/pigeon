package com.dianping.pigeon.remoting.common.codec.thrift.annotation;

import com.facebook.swift.codec.ThriftCodecManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @author qi.yin
 *         2016/05/24  上午10:45.
 */
public class ThriftServerMetadata {

    private final String serverType;
    private final String serverName;

    private final ThriftCodecManager codecManager = new ThriftCodecManager();
    private Map<String, ThriftMethodProcessor> methodProcessors;

    public ThriftServerMetadata(Class<?> serverType, String serverName) {
        this.serverName = serverName;
        Map<String, ThriftMethodProcessor> processorMap = newHashMap();

        ThriftServiceMetadata serviceMetadata = new ThriftServiceMetadata(serverType, codecManager.getCatalog());
        this.serverType = serviceMetadata.getName();
        for (ThriftMethodMetadata methodMetadata : serviceMetadata.getMethods().values()) {
            String methodName = methodMetadata.getName();
            ThriftMethodProcessor methodProcessor = new ThriftMethodProcessor(serverType, serviceMetadata.getName(), methodMetadata, codecManager);
            if (processorMap.containsKey(methodName)) {
                throw new IllegalArgumentException("Multiple @ThriftMethod-annotated methods named '" + methodName + "' found in the given services");
            }
            processorMap.put(methodName, methodProcessor);
        }

        methodProcessors = ImmutableMap.copyOf(processorMap);
        if (null == methodProcessors) {
            throw new IllegalArgumentException("no @ThriftMethod-annotated methods.");
        }

    }

    public String getServerType() {
        return serverType;
    }

    public String getServerName() {
        return serverName;
    }

    public Map<String, ThriftMethodProcessor> getMethodProcessors() {
        return methodProcessors;
    }

    public ThriftMethodProcessor getMethodProcessor(String method) {
        return methodProcessors.get(method);
    }
}
