package com.dianping.pigeon.test.benchmark.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.avatar.tracker.ExecutionContextHolder;
import com.dianping.avatar.tracker.TrackerContext;
import com.dianping.cache.core.CacheCallback;
import com.dianping.cache.exception.CacheException;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service(url = "com.dianping.cache.test.DCacheDemoService", interfaceClass = CacheTestService.class)
public class DCacheTestService extends AbstractCacheTestService {

	@Resource
	private CacheService cacheService;

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	public boolean setKeyValue(String key, String value) {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		try {
			return cacheService.set(cacheKey, value);
		} catch (CacheException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getKeyValue(String key) {
		TrackerContext ctxt = new TrackerContext();
		ctxt.setTrackRequired(true);
		ExecutionContextHolder.setTrackerContext(ctxt);

		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.get(cacheKey);
	}

	@Override
	public List<Object> bulkGetKeyValue(String keys) {
		List<CacheKey> cacheKeys = new ArrayList<CacheKey>();
		if (StringUtils.isNotBlank(keys)) {
			String[] keyArray = keys.split(",");
			for (String key : keyArray) {
				if (StringUtils.isNotBlank(key)) {
					CacheKey cacheKey = new CacheKey("mydcache", key);
					cacheKeys.add(cacheKey);
				}
			}
		}
		if (!CollectionUtils.isEmpty(cacheKeys)) {
			return cacheService.mGet(cacheKeys, false);
		}
		return null;
	}

	@Override
	public boolean deleteKey(String key) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.delete(cacheKey);
	}

	public boolean addKeyValue(String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.addIfAbsent(cacheKey, value);
	}

	public void randomGet() {
		getKeyValue("k-" + (random.nextDouble() * rows));
	}

	public boolean asyncDeleteKey(String key) throws CacheException, InterruptedException, ExecutionException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.asyncDelete(cacheKey).get();
	}

	public Future<String> asyncGetKeyValueByFuture(String key) throws CacheException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.asyncGet(cacheKey);
	}

	public Future<Boolean> asyncSetKeyValueByFuture(String key, String value) throws CacheException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.asyncSet(cacheKey, value);
	}

	public boolean asyncSetKeyValue(String key, String value) throws CacheException, InterruptedException,
			ExecutionException {
		Future<Boolean> future = asyncSetKeyValueByFuture(key, value);
		return future.get();
	}

	public String asyncGetKeyValue(String key) throws Exception {
		Future<String> future = asyncGetKeyValueByFuture(key);
		return future.get();
	}

	public void asyncGetKeyValueByCallback(String key) {
		asyncGetKeyValueByCallback(key, true);
	}

	private void asyncGetKeyValueByCallback(String key, final boolean printResult) {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		cacheService.asyncGet(cacheKey, new CacheCallback<String>() {

			@Override
			public void onSuccess(String result) {
				if (printResult) {
					System.out.print(result);
				}
			}

			@Override
			public void onFailure(String msg, Throwable e) {
				if (printResult) {
					System.out.print(msg);
					if (e != null) {
						e.printStackTrace();
					}
				}
			}

		});
	}

	public void concurrentAsyncGet(int threads, final int rows) {
		executor = Executors.newFixedThreadPool(threads);
		isCancel = false;
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (!isCancel) {
						Transaction t = Cat.newTransaction("cache", "cache");
						for (int i = 0; i < 500; i++) {
							asyncGetKeyValueByCallback("k-" + Math.abs((int) (random.nextDouble() * rows)), false);
						}
						t.setStatus(Message.SUCCESS);
						t.complete();
					}
				}
			});
		}
	}

	private void asyncSetKeyValueByCallback(String key, String value, final boolean printResult) {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		cacheService.asyncSet(cacheKey, value, new CacheCallback<Boolean>() {

			@Override
			public void onSuccess(Boolean result) {
				if (printResult) {
					System.out.print(result);
				}
			}

			@Override
			public void onFailure(String msg, Throwable e) {
				if (printResult) {
					System.out.print(msg);
					if (e != null) {
						e.printStackTrace();
					}
				}
			}

		});
	}

	public void asyncSetKeyValueByCallback(String key, String value) {
		asyncSetKeyValueByCallback(key, value, true);
	}
	
	public void concurrentAsyncSet(int threads, final int rows, final int size) {
		executor = Executors.newFixedThreadPool(threads);
		isCancel = false;
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (!isCancel) {
						Transaction t = Cat.newTransaction("cache", "cache");
						for (int i = 0; i < 500; i++) {
							asyncSetKeyValueByCallback("k-" + Math.abs((int) (random.nextDouble() * rows)),
									StringUtils.leftPad("" + i, size), false);
						}
						t.setStatus(Message.SUCCESS);
						t.complete();
					}
				}
			});
		}
	}

}
