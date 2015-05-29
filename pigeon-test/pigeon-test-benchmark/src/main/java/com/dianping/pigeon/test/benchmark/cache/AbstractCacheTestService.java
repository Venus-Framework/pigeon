package com.dianping.pigeon.test.benchmark.cache;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;

import com.dianping.cache.exception.CacheException;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public abstract class AbstractCacheTestService implements CacheTestService {

	protected static Random random = new Random();
	protected static int rows = 0;
	volatile boolean isCancel = false;
	ExecutorService executor = null;

	public void concurrentGet(int threads, final int rows) {
		executor = Executors.newFixedThreadPool(threads);
		isCancel = false;
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (!isCancel) {
						Transaction t = Cat.newTransaction("cache", "cache");
						for (int i = 0; i < 500; i++) {
							getKeyValue("k-" + Math.abs((int) (random.nextDouble() * rows)));
						}
						t.setStatus(Message.SUCCESS);
						t.complete();
					}
				}
			});
		}
	}

	public void concurrentSet(int threads, final int rows, final int size) {
		executor = Executors.newFixedThreadPool(threads);
		isCancel = false;
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (!isCancel) {
						Transaction t = Cat.newTransaction("cache", "cache");
						for (int i = 0; i < 500; i++) {
							setKeyValue("k-" + Math.abs((int) (random.nextDouble() * rows)),
									StringUtils.leftPad("" + i, size));
						}
						t.setStatus(Message.SUCCESS);
						t.complete();
					}
				}
			});
		}
	}

	@Override
	public void init(int rows, int size) throws CacheException, TimeoutException {
		clear();
		for (int i = 0; i < rows; i++) {
			this.setKeyValue("k-" + i, StringUtils.leftPad("" + i, size, '#'));
		}
		AbstractCacheTestService.rows = rows;
	}

	@Override
	public void clear() throws CacheException, TimeoutException {
		for (int i = 0; i < rows; i++) {
			this.deleteKey("k-" + i);
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
