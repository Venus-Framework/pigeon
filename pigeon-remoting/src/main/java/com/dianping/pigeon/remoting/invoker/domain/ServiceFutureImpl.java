/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.domain;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.dianping.dpsf.async.ServiceFuture;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: ServiceFutureImpl.java, v 0.1 2013-6-20 下午5:50:31 jianhuihuang
 *          Exp $
 */
public class ServiceFutureImpl extends CallbackFuture implements ServiceFuture {

	private static final Logger logger = LoggerLoader.getLogger(ServiceFutureImpl.class);

	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(Monitor.class).getLogger();

	private long timeout = Long.MAX_VALUE;

	public ServiceFutureImpl(long timeout) {
		super();
		this.timeout = timeout;
	}

	@Override
	public Object _get() throws InterruptedException {
		return _get(this.timeout);
	}

	@Override
	public Object _get(long timeoutMillis) throws InterruptedException {
		try {
			InvocationResponse res = super.get(timeoutMillis);
			if (res.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
				return res.getReturn();
			} else if (res.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
				logger.error(res.getCause());
				RuntimeException dpsfE = new RuntimeException(res.getCause());
				monitorLogger.logError(dpsfE);
				throw dpsfE;
			} else if (res.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
				RuntimeException dpsfE = new RuntimeException((Throwable) res.getReturn());
				monitorLogger.logError(dpsfE);
				throw dpsfE;
			} else {
				throw new RuntimeException("error messageType:" + res.getMessageType());
			}

		} catch (Exception e) {
			RuntimeException dpsfE = new RuntimeException(e);
			monitorLogger.logError(dpsfE);
			throw dpsfE;
		}
	}

	@Override
	public Object _get(long timeout, TimeUnit unit) throws InterruptedException {
		return _get(unit.toMillis(timeout));
	}

	protected void processContext() {}
	
}
