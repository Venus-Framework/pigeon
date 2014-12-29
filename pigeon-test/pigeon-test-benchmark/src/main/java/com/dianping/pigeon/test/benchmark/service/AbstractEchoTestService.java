package com.dianping.pigeon.test.benchmark.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dianping.pigeon.remoting.invoker.util.InvokerHelper;

public abstract class AbstractEchoTestService implements EchoTestService {

	volatile boolean isCancel = false;

	ExecutorService executor = null;

	abstract EchoService getEchoService();

	public void concurrentFindUsers(final int threads, final int count, final int timeout) {
		executor = Executors.newFixedThreadPool(threads);
		this.isCancel = false;
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					findUsers(count, timeout);
				}
			});
		}
	}

	private void findUsers(final int count, final int timeout) {
		while (!isCancel) {
			InvokerHelper.setTimeout(timeout);
			try {
				getEchoService().findUsers(count);
			} catch (RuntimeException e) {
			}
		}
	}

	public void concurrentGetNow(final int threads, final int timeout) {
		executor = Executors.newFixedThreadPool(threads);
		this.isCancel = false;
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					getNow(timeout);
				}
			});
		}
	}

	private void getNow(final int timeout) {
		while (!isCancel) {
			InvokerHelper.setTimeout(timeout);
			try {
				getEchoService().now();
			} catch (RuntimeException e) {
			}
		}
	}

	@Override
	public void cancel() {
		this.isCancel = true;
		if (executor != null) {
			executor.shutdown();
		}
	}
}
