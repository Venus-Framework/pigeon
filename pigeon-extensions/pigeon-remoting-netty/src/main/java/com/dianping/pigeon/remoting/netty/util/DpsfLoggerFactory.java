package com.dianping.pigeon.remoting.netty.util;

import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import com.dianping.pigeon.log.LoggerLoader;

public class DpsfLoggerFactory extends InternalLoggerFactory {

    @Override
    public InternalLogger newInstance(String name) {
        org.apache.log4j.Logger logger = LoggerLoader.getLogger("netty");
        return new DpsfLogger(logger);
    }

}
