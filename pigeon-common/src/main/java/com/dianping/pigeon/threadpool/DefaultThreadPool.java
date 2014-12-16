/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

public class DefaultThreadPool implements ThreadPool {

	private String name;

	private ThreadPoolExecutor executor;
	private DefaultThreadFactory factory;

	public DefaultThreadPool(String poolName) {
		this.name = poolName;
		this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(new DefaultThreadFactory(poolName));
	}

	public DefaultThreadPool(String poolName, int corePoolSize, int maximumPoolSize) {

		this(poolName, corePoolSize, maximumPoolSize, new SynchronousQueue<Runnable>());
	}

	public DefaultThreadPool(String poolName, int corePoolSize, int maximumPoolSize, BlockingQueue<Runnable> workQueue) {
		this(poolName, corePoolSize, maximumPoolSize, workQueue, new AbortPolicy());
	}

	public DefaultThreadPool(String poolName, int corePoolSize, int maximumPoolSize, BlockingQueue<Runnable> workQueue,
			RejectedExecutionHandler handler) {
		this.name = poolName;
		this.factory = new DefaultThreadFactory(this.name);
		this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS, workQueue,
				this.factory, handler);
	}

	public void execute(Runnable run) {
		this.executor.execute(run);
	}

	public <T> Future<T> submit(Callable<T> call) {
		return this.executor.submit(call);
	}

	public Future<?> submit(Runnable run) {
		return this.executor.submit(run);
	}

	public ThreadPoolExecutor getExecutor() {
		return this.executor;
	}

}
