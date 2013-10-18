/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component.async;

import java.util.concurrent.TimeUnit;

import com.dianping.dpsf.component.DPSFResponse;

public interface CallFuture extends Call {

	DPSFResponse get() throws InterruptedException;

	DPSFResponse get(long timeoutMillis) throws InterruptedException;

	DPSFResponse get(long timeout, TimeUnit unit) throws InterruptedException;

	boolean cancel();

	boolean isCancelled();

	boolean isDone();

}
