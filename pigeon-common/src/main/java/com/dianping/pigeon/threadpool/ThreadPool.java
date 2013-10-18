/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public interface ThreadPool {

	public void execute(Runnable run);

	public <T> Future<T> submit(Callable<T> call);

	public Future<?> submit(Runnable run);

	public ThreadPoolExecutor getExecutor();

}
