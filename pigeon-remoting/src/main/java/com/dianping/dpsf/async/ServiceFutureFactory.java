/**
 * 
 */
package com.dianping.dpsf.async;

import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.invoker.concurrent.FutureFactory;

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

	public static ServiceFuture getFuture() {
		Future future = FutureFactory.getFuture();
		if (future != null) {
			return new ServiceFutureWrapper(future);
		}
		return null;
	}

	public static void setFuture(ServiceFuture future) {
	}

	public static void remove() {
		FutureFactory.remove();
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
