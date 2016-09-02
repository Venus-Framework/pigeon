package com.dianping.pigeon.remoting.invoker.concurrent;

public interface InvocationCallback {

	/**
	 * 正常结果返回
	 * 
	 * @param result
	 */
	public void onSuccess(Object result);

	public void onFailure(Throwable exception);
}
