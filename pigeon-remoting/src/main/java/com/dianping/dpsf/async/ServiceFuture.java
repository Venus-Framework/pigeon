/**
 * 
 */
package com.dianping.dpsf.async;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Title: DPSFFuture.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author saber miao
 * @version 1.0
 * @created 2011-3-22 上午12:17:02
 */
public interface ServiceFuture {

	/**
	 * 超时时间为Spring中配置的timout时间
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	Object _get() throws InterruptedException;

	/**
	 * @param timeoutMillis
	 *            阻塞超时时间，单位毫秒
	 * @return
	 * @throws InterruptedException
	 */
	Object _get(long timeoutMillis) throws InterruptedException;

	/**
	 * @param timeout
	 *            阻塞超时时间，单位自定义
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	Object _get(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * @return 是否完成
	 */
	boolean isDone();

}
