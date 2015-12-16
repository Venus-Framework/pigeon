package com.dianping.pigeon.remoting.invoker.proxy;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

/**
 * Created by chenchongze on 15/12/16.
 */
public class ServiceProxyLoader {

    private static ServiceProxy serviceProxy = ExtensionLoader.getExtension(ServiceProxy.class);
    private static final Logger logger = LoggerLoader.getLogger(ServiceProxyLoader.class);

    static {
        if (serviceProxy == null) {
            serviceProxy = new DefaultServiceProxy();
        }
        logger.info("serviceProxy:" + serviceProxy);
        serviceProxy.init();
    }

    public static ServiceProxy getServiceProxy() {
        return serviceProxy;
    }
}
