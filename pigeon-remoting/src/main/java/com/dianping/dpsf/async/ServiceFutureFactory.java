/**
 * 
 */
package com.dianping.dpsf.async;

import com.dianping.pigeon.log.LoggerLoader;

import org.apache.logging.log4j.Logger;

/**
 * <p>
 * Title: ServiceFutureFactory.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author saber miao
 * @version 1.0
 * @created 2011-3-22 上午09:45:27
 */
public class ServiceFutureFactory {

	private static Logger log = LoggerLoader.getLogger(ServiceFutureFactory.class);
	private static ThreadLocal<ServiceFuture> threadFuture = new ThreadLocal<ServiceFuture>();

	public static ServiceFuture getFuture() {
		ServiceFuture future = threadFuture.get();
		threadFuture.remove();
		return future;
	}

	public static void setFuture(ServiceFuture future) {
//		if (threadFuture.get() != null) {
//			threadFuture.remove();
//			String msg = "you must call \"ServiceFutureFactory.getFuture()\" before second call service if you use future call";
//			log.error(msg);
//			throw new InvalidParameterException(msg);
//		}
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
	 */
	public static <T> T getResult(Class<T> res) throws InterruptedException {
		return (T) getFuture()._get();
	}

	/**
	 * 直接返回调用结果，用于异步调用配置情况下的同步调用
	 * 
	 * @return 调用结果
	 * @throws InterruptedException
	 */
	public static Object getResult() throws InterruptedException {
		return getFuture()._get();
	}

}
