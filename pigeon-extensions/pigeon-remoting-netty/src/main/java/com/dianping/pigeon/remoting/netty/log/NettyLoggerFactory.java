package com.dianping.pigeon.remoting.netty.log;

import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import com.dianping.pigeon.log.LoggerLoader;

public class NettyLoggerFactory extends InternalLoggerFactory {

    @Override
    public InternalLogger newInstance(String name) {
        org.apache.log4j.Logger logger = LoggerLoader.getLogger("netty");
        return new NettyLogger(logger);
    }

}
