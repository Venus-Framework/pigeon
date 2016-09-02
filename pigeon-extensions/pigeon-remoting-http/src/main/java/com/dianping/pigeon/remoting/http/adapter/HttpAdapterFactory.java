package com.dianping.pigeon.remoting.http.adapter;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;

import com.dianping.pigeon.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenchongze on 16/1/20.
 */
public class HttpAdapterFactory {

    private static Logger logger = LoggerLoader.getLogger(HttpAdapterFactory.class);
    private static Map<String, HttpAdapter> httpAdapters = new HashMap<String, HttpAdapter>();

    public static Map<String, HttpAdapter> getHttpAdapters() {
        return httpAdapters;
    }

    public static void registerHttpAdapter(String serviceName, HttpAdapter httpAdapter) {
        Map<String, ProviderConfig<?>> allServiceProviders = ServicePublisher.getAllServiceProviders();
        if(!allServiceProviders.containsKey(serviceName)) {
            logger.warn("service " + serviceName + " not exists or not published!");
        }
        httpAdapters.put(serviceName, httpAdapter);
        logger.info("register httpAdapter: " + httpAdapter.getClass().getCanonicalName() + " for service: " + serviceName);
    }

    public static void unregisterHttpAdapter(String serviceName) {
        httpAdapters.remove(serviceName);
        logger.warn("unregister httpAdapter for service: " + serviceName);
    }

}
