/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component.async;

import org.apache.log4j.Logger;

import com.dianping.pigeon.exception.PigeonRuntimeException;

public final class ServiceFutureFactory {

	private static final Logger log = Logger.getLogger(ServiceFutureFactory.class);

	private static ThreadLocal<ServiceFuture> threadFuture = new ThreadLocal<ServiceFuture>();

	private static ServiceFuture getFuture() {

		ServiceFuture future = threadFuture.get();
		threadFuture.remove();
		return future;
	}

	public static void setFuture(ServiceFuture future) throws PigeonRuntimeException {

		if (threadFuture.get() != null) {
			threadFuture.remove();
			String msg = "you must call \"ServiceFutureFactory.getFuture()\" before second call service if you use future call";
			log.error(msg);
			throw new PigeonRuntimeException(msg);
		}
		threadFuture.set(future);
	}

	public static void remove() {
		threadFuture.remove();
	}

	/**
	 * 直接返回调用结果，用于异步调用配置情况下的同步调用
	 * 
	 * @param <T>
	 *            返回值类型
	 * @param res
	 *            返回值类
	 * @return 调用结果
	 * @throws InterruptedException
	 * @throws PigeonRuntimeException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getResult(Class<T> res) throws InterruptedException, PigeonRuntimeException {
		return (T) getFuture()._get();
	}

	/**
	 * 直接返回调用结果，用于异步调用配置情况下的同步调用
	 * 
	 * @return 调用结果
	 * @throws InterruptedException
	 * @throws PigeonRuntimeException
	 */
	public static Object getResult() throws InterruptedException, PigeonRuntimeException {
		return getFuture()._get();
	}

}
