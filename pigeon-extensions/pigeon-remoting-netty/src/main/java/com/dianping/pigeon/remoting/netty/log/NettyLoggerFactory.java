package com.dianping.pigeon.remoting.netty.log;

import com.dianping.pigeon.log.LoggerLoader;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

public class NettyLoggerFactory extends InternalLoggerFactory {

    @Override
    public InternalLogger newInstance(String name) {
        org.apache.logging.log4j.Logger logger = LoggerLoader.getLogger("netty");
        return new NettyLogger(logger);
    }

}
