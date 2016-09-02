package com.dianping.pigeon.demo;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.concurrent.InvocationCallback;

public class EchoServiceCallback implements InvocationCallback {

	private final Logger logger = LoggerLoader.getLogger(EchoServiceCallback.class);

	@Override
	public void onSuccess(Object result) {
		System.out.println("callback onSuccess:" + result);		
	}

	@Override
	public void onFailure(Throwable exception) {
		System.out.println("callback onFailure:");	
		exception.printStackTrace();
	}

}
