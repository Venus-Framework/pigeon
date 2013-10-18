/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.component.context;

import java.util.concurrent.Future;

public class RequestContext {

	private Future<?> future;
	private Thread thread;
	private String host;

	public RequestContext(String host) {
		this.host = host;
	}

	/**
	 * @return the future
	 */
	public Future<?> getFuture() {
		return future;
	}

	/**
	 * @param future
	 *            the future to set
	 */
	public void setFuture(Future<?> future) {
		this.future = future;
	}

	/**
	 * @return the thread
	 */
	public Thread getThread() {
		return thread;
	}

	public String getHost() {
		return host;
	}

}
