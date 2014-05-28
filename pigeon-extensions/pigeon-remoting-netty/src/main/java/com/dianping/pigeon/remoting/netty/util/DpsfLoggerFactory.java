package com.dianping.pigeon.remoting.netty.util;

import org.jboss.netty.logging.Log4JLogger;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import com.dianping.dpsf.DPSFLog;

public class DpsfLoggerFactory extends InternalLoggerFactory {

    @Override
    public InternalLogger newInstance(String name) {
        org.apache.log4j.Logger logger = DPSFLog.getLogger();
        return new Log4JLogger(logger);
    }

}
