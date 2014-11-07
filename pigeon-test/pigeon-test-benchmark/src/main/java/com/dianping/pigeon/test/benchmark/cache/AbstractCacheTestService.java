package com.dianping.pigeon.test.benchmark.cache;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

public abstract class AbstractCacheTestService implements CacheTestService {

	protected static Random random = new Random();
	protected static int rows = 0;

	public void concurrentGet(int threads) {
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (true) {
						getKeyValue("k-" + Math.abs((int)(random.nextDouble() * rows)));
					}
				}
			});
		}
	}

	@Override
	public void init(int rows) {
		clear();
		for (int i = 0; i < rows; i++) {
			this.setKeyValue("k-" + i, StringUtils.leftPad("" + i, 25));
		}
		AbstractCacheTestService.rows = rows;
	}

	@Override
	public void clear() {
		for (int i = 0; i < rows; i++) {
			this.removeKey("k-" + i);
		}
	}

}
