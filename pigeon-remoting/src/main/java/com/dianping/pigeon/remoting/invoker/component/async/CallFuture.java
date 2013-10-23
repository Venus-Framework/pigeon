/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component.async;

import java.util.concurrent.TimeUnit;

import com.dianping.pigeon.component.invocation.InvocationResponse;

public interface CallFuture extends Call {

	InvocationResponse get() throws InterruptedException;

	InvocationResponse get(long timeoutMillis) throws InterruptedException;

	InvocationResponse get(long timeout, TimeUnit unit) throws InterruptedException;

	boolean cancel();

	boolean isCancelled();

	boolean isDone();

}
