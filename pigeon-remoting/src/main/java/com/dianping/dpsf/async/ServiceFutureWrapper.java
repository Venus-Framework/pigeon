/**
 * 
 */
package com.dianping.dpsf.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;

public class ServiceFutureWrapper implements ServiceFuture {

	private Future future;

	public ServiceFutureWrapper(Future future) {
		this.future = future;
	}

	@Override
	public Object _get() throws InterruptedException {
		try {
			return future.get();
		} catch (ExecutionException e) {
			throw new RpcException(e);
		}
	}

	@Override
	public Object _get(long timeoutMillis) throws InterruptedException {
		return _get(timeoutMillis, TimeUnit.MILLISECONDS);
	}

	@Override
	public Object _get(long timeout, TimeUnit unit) throws InterruptedException {
		try {
			return future.get(timeout, unit);
		} catch (ExecutionException e) {
			throw new RpcException(e);
		} catch (TimeoutException e) {
			throw new RequestTimeoutException(e);
		}
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

}
