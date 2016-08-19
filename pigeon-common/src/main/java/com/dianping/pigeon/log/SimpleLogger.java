package com.dianping.pigeon.log;

public class SimpleLogger implements Logger {

	private org.apache.logging.log4j.Logger LOG;

	private static volatile boolean isDebugEnabled = false;

	public SimpleLogger(org.apache.logging.log4j.Logger logger) {
		this.LOG = logger;
	}

	@Override
	public void debug(Object message) {
		if (this.isDebugEnabled()) {
			this.LOG.debug(message);
		}
	}

	@Override
	public void debug(Object message, Throwable t) {
		if (this.isDebugEnabled()) {
			this.LOG.debug(message, t);
		}
	}

	@Override
	public void debug(String message) {
		if (this.isDebugEnabled()) {
			this.LOG.debug(message);
		}
	}

	@Override
	public void debug(String message, Object... params) {
		if (this.isDebugEnabled()) {
			this.LOG.debug(message, params);
		}
	}

	@Override
	public void debug(String message, Throwable t) {
		if (this.isDebugEnabled()) {
			this.LOG.debug(message, t);
		}
	}

	@Override
	public void error(Object message) {
		this.LOG.error(message);
	}

	@Override
	public void error(Object message, Throwable t) {
		this.LOG.error(message, t);
	}

	@Override
	public void error(String message) {
		this.LOG.error(message);
	}

	@Override
	public void error(String message, Object... params) {
		this.LOG.error(message, params);
	}

	@Override
	public void error(String message, Throwable t) {
		this.LOG.error(message, t);
	}

	@Override
	public void fatal(Object message) {
		this.LOG.fatal(message);
	}

	@Override
	public void fatal(Object message, Throwable t) {
		this.LOG.fatal(message, t);
	}

	@Override
	public void fatal(String message) {
		this.LOG.fatal(message);
	}

	@Override
	public void fatal(String message, Object... params) {
		this.LOG.fatal(message, params);
	}

	@Override
	public void fatal(String message, Throwable t) {
		this.LOG.fatal(message, t);
	}

	@Override
	public String getName() {
		return this.LOG.getName();
	}

	@Override
	public void info(Object message) {
		this.LOG.info(message);
	}

	@Override
	public void info(Object message, Throwable t) {
		this.LOG.info(message, t);
	}

	@Override
	public void info(String message) {
		this.LOG.info(message);
	}

	@Override
	public void info(String message, Object... params) {
		this.LOG.info(message, params);
	}

	@Override
	public void info(String message, Throwable t) {
		this.LOG.info(message, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}

	public static void setDebugEnabled(boolean enabled) {
		isDebugEnabled = enabled;
	}

	@Override
	public boolean isErrorEnabled() {
		return this.LOG.isErrorEnabled();
	}

	@Override
	public boolean isFatalEnabled() {
		return this.LOG.isFatalEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return this.LOG.isInfoEnabled();
	}

	@Override
	public boolean isTraceEnabled() {
		return this.LOG.isTraceEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return this.LOG.isWarnEnabled();
	}

	@Override
	public void trace(Object message) {
		this.LOG.trace(message);
	}

	@Override
	public void trace(Object message, Throwable t) {
		this.LOG.trace(message, t);
	}

	@Override
	public void trace(String message) {
		this.LOG.trace(message);
	}

	@Override
	public void trace(String message, Object... params) {
		this.LOG.trace(message, params);
	}

	@Override
	public void trace(String message, Throwable t) {
		this.LOG.trace(message, t);
	}

	@Override
	public void warn(Object message) {
		this.LOG.warn(message);
	}

	@Override
	public void warn(Object message, Throwable t) {
		this.LOG.warn(message, t);
	}

	@Override
	public void warn(String message) {
		this.LOG.warn(message);
	}

	@Override
	public void warn(String message, Object... params) {
		this.LOG.warn(message, params);
	}

	@Override
	public void warn(String message, Throwable t) {
		this.LOG.warn(message, t);
	}

}
