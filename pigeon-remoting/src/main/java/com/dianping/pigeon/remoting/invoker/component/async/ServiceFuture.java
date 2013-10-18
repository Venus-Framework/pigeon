/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component.async;

import java.util.concurrent.TimeUnit;

import com.dianping.pigeon.exception.PigeonRuntimeException;

public interface ServiceFuture {

	/**
	 * 超时时间为Spring中配置的timout时间
	 * 
	 * @return
	 * @throws InterruptedException
	 * @throws PigeonRuntimeException
	 */
	Object _get() throws InterruptedException, PigeonRuntimeException;

	/**
	 * @param timeoutMillis
	 *            阻塞超时时间，单位毫秒
	 * @return
	 * @throws InterruptedException
	 * @throws PigeonRuntimeException
	 */
	Object _get(long timeoutMillis) throws InterruptedException, PigeonRuntimeException;

	/**
	 * @param timeout
	 *            阻塞超时时间，单位自定义
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @throws PigeonRuntimeException
	 */
	Object _get(long timeout, TimeUnit unit) throws InterruptedException, PigeonRuntimeException;

	/**
	 * @return 是否完成
	 */
	boolean isDone();

}
