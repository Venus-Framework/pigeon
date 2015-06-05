package com.dianping.pigeon.remoting.netty.log;


import org.apache.logging.log4j.Logger;
import org.jboss.netty.logging.AbstractInternalLogger;

public class NettyLogger extends AbstractInternalLogger {

    private final Logger logger;

    public NettyLogger(Logger logger) {
        this.logger = logger;
    }

    public void debug(String msg) {
        logger.debug(msg);
    }

    public void debug(String msg, Throwable cause) {
        logger.debug(msg, cause);
    }

    public void error(String msg) {
        logger.error(msg);
    }

    public void error(String msg, Throwable cause) {
        logger.error(msg, cause);
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void info(String msg, Throwable cause) {
        logger.info(msg, cause);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return true;
    }

    public void warn(String msg) {
        logger.warn(msg);
    }

    public void warn(String msg, Throwable cause) {
        logger.warn(msg, cause);
    }

    @Override
    public String toString() {
        return String.valueOf(logger.getName());
    }
}
