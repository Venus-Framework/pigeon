/**
 * 
 */
package com.dianping.dpsf.async;

import com.dianping.dpsf.exception.DPSFException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.invoker.concurrent.InvocationCallback;

/**
 * <p>
 * Title: DPSFCallback.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author saber miao
 * @version 1.0
 * @created 2011-3-22 上午12:12:51
 * @deprecated
 * @see com.dianping.pigeon.remoting.invoker.concurrent.InvocationCallback
 */
@Deprecated
public abstract class ServiceCallback implements InvocationCallback {

	public void onSuccess(Object result) {
		this.callback(result);
	}

	public void onFailure(Throwable exception) {
		if (exception instanceof RpcException) {
			this.frameworkException(new DPSFException(exception));
		} else if (exception instanceof Exception) {
			this.serviceException((Exception) exception);
		}
	}

	/**
	 * 正常结果返回
	 * 
	 * @param result
	 */
	public abstract void callback(Object result);

	/**
	 * 后端应用Service抛出的异常
	 * 
	 * @param e
	 */
	public abstract void serviceException(Exception e);

	/**
	 * 通信框架发生异常，没有必要可以不处理
	 * 
	 * @param e
	 */
	public abstract void frameworkException(DPSFException e);
}
