/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component.async;

import com.dianping.pigeon.exception.PigeonRuntimeException;

public interface ServiceCallback {

	/**
	 * 正常结果返回
	 * 
	 * @param result
	 */
	void callback(Object result);

	/**
	 * 后端应用Service抛出的异常
	 * 
	 * @param e
	 */
	void serviceException(Exception e);

	/**
	 * 通信框架发生异常，没有必要可以不处理
	 * 
	 * @param e
	 */
	void frameworkException(PigeonRuntimeException e);
}
