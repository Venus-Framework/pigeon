/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component.async;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.dianping.dpsf.component.DPSFResponse;
import com.dianping.pigeon.exception.PigeonRuntimeException;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.util.Constants;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: ServiceFutureImpl.java, v 0.1 2013-6-20 下午5:50:31 jianhuihuang
 *          Exp $
 */
public class ServiceFutureImpl extends CallbackFuture implements ServiceFuture {

	private static final Logger logger = Logger.getLogger(ServiceFutureImpl.class);

	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(MonitorLogger.class);

	private long timeout = Long.MAX_VALUE;

	public ServiceFutureImpl(long timeout) {
		super();
		this.timeout = timeout;
	}

	@Override
	public Object _get() throws InterruptedException, PigeonRuntimeException {
		return _get(this.timeout);
	}

	@Override
	public Object _get(long timeoutMillis) throws InterruptedException, PigeonRuntimeException {
		try {
			DPSFResponse res = super.get(timeoutMillis);
			if (res.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
				return res.getReturn();
			} else if (res.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
				logger.error(res.getCause());
				PigeonRuntimeException dpsfE = new PigeonRuntimeException(res.getCause());
				monitorLogger.logError(dpsfE);
				throw dpsfE;
			} else if (res.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
				PigeonRuntimeException dpsfE = new PigeonRuntimeException((Throwable) res.getReturn());
				monitorLogger.logError(dpsfE);
				throw dpsfE;
			} else {
				throw new PigeonRuntimeException("error messageType:" + res.getMessageType());
			}

		} catch (Exception e) {
			PigeonRuntimeException dpsfE = new PigeonRuntimeException(e);
			monitorLogger.logError(dpsfE);
			throw dpsfE;
		}
	}

	@Override
	public Object _get(long timeout, TimeUnit unit) throws InterruptedException, PigeonRuntimeException {
		return _get(unit.toMillis(timeout));
	}

}
