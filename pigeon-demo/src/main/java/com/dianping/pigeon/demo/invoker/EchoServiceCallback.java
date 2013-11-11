package com.dianping.pigeon.demo.invoker;

import org.apache.log4j.Logger;

import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;

public class EchoServiceCallback implements ServiceCallback {

	private static final Logger logger = LoggerLoader.getLogger(EchoServiceCallback.class);

	@Override
	public void callback(Object result) {
		System.out.println("callback:" + result);
	}

	@Override
	public void serviceException(Exception e) {
		logger.error("", e);
	}

	@Override
	public void frameworkException(RuntimeException e) {
		logger.error("", e);
	}

}
