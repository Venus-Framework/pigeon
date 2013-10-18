package com.dianping.pigeon.demo.invoker;

import org.apache.log4j.Logger;

import com.dianping.pigeon.exception.PigeonRuntimeException;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;

public class EchoServiceCallback implements ServiceCallback {

	private static final Logger logger = Logger.getLogger(EchoServiceCallback.class);

	@Override
	public void callback(Object result) {
		System.out.println("callback:" + result);
	}

	@Override
	public void serviceException(Exception e) {
		logger.error("", e);
	}

	@Override
	public void frameworkException(PigeonRuntimeException e) {
		logger.error("", e);
	}

}
