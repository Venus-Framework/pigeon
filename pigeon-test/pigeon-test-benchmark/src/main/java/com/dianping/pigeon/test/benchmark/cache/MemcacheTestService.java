package com.dianping.pigeon.test.benchmark.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.cache.CacheException;
import org.springframework.util.CollectionUtils;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.avatar.tracker.ExecutionContextHolder;
import com.dianping.avatar.tracker.TrackerContext;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service(url = "com.dianping.cache.test.MemcacheDemoService", interfaceClass = CacheTestService.class)
public class MemcacheTestService extends AbstractCacheTestService {

	@Resource
	private CacheService cacheService;

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	public boolean setKeyValue(String key, String value) {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
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

		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.get(cacheKey);
	}

	@Override
	public List<Object> bulkGetKeyValue(String keys) {
		List<CacheKey> cacheKeys = new ArrayList<CacheKey>();
		if (StringUtils.isNotBlank(keys)) {
			String[] keyArray = keys.split(",");
			for (String key : keyArray) {
				if (StringUtils.isNotBlank(key)) {
					CacheKey cacheKey = new CacheKey("mymemcache", key);
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
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.delete(cacheKey);
	}

	public boolean addKeyValue(String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.addIfAbsent(cacheKey, value);
	}

	public void randomGet() {
		getKeyValue("k-" + (random.nextDouble() * rows));
	}

	@Override
	public boolean asyncDeleteKey(String key) throws CacheException, InterruptedException, ExecutionException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.asyncDelete(cacheKey).get();
	}

	public Future<String> asyncGetKeyValueByFuture(String key) throws CacheException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.asyncGet(cacheKey);
	}

	public Future<Boolean> asyncSetKeyValueByFuture(String key, String value) throws CacheException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
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
							try {
								asyncGetKeyValueByFuture("k-" + Math.abs((int) (random.nextDouble() * rows)));
							} catch (CacheException e) {
								e.printStackTrace();
							}
						}
						t.setStatus(Message.SUCCESS);
						t.complete();
					}
				}
			});
		}
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
							try {
								asyncSetKeyValueByFuture("k-" + Math.abs((int) (random.nextDouble() * rows)),
										StringUtils.leftPad("" + i, size));
							} catch (CacheException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						t.setStatus(Message.SUCCESS);
						t.complete();
					}
				}
			});
		}
	}

	@Override
	public void asyncGetKeyValueByCallback(String key) {

	}

}
