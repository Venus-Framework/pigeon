package com.dianping.pigeon.governor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.cache.core.CASResponse;
import com.dianping.cache.core.CASValue;
import com.dianping.cache.core.CacheCallback;
import com.dianping.cache.exception.CacheException;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service(url = "com.dianping.cache.test.DCacheDemoService")
public class DCacheDemoService implements CacheDemoService {

	protected static Random random = new Random();
	protected static int rows = 0;
	volatile boolean isCancel = false;
	ExecutorService executor = null;
	volatile int batch = 50;

	@Resource
	private CacheService cacheService;

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	public void concurrentGet(int threads, final int rows) {
		executor = Executors.newFixedThreadPool(threads);
		isCancel = false;
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					String result = null;
					while (!isCancel) {
						Transaction t = Cat.newTransaction("cache", "cache");
						for (int i = 0; i < 500; i++) {
							result = getKeyValue("k-" + Math.abs((int) (random.nextDouble() * rows)));
						}
						t.setStatus(Message.SUCCESS);
						t.complete();
					}
				}
			});
		}
	}

    @Override
    public void concurrentMultiGet(int threads, int rows) {
        executor = Executors.newFixedThreadPool(threads);
        isCancel = false;
        for (int i = 0; i < threads; i++) {
            executor.submit(new Runnable() {

                @Override
                public void run() {
                    Object result = null;
                    while (!isCancel) {
                        Transaction t = Cat.newTransaction("cache", "cache");
                        for (int i = 0; i < 100; i++) {
                            result = getMultiKeyValue(batch);
                        }
                        t.setStatus(Message.SUCCESS);
                        t.complete();
                    }
                }

            });
        }
    }
    
    private Object getMultiKeyValue(int batch) {
        List<CacheKey> keys = new ArrayList<CacheKey>(batch);
        for(int i=0; i<batch; i++) {
            CacheKey cacheKey = new CacheKey("mydcache", "k-" + Math.abs((int) (random.nextDouble() * rows)));
            keys.add(cacheKey);
        }
        return cacheService.mGetWithNonExists(keys);
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
							try {
								setKeyValue("k-" + Math.abs((int) (random.nextDouble() * rows)),
										StringUtils.leftPad("" + i, size));
							} catch (CacheException e) {
								e.printStackTrace();
							} catch (TimeoutException e) {
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

	@Override
	public void init(int rows, int size) throws CacheException, TimeoutException {
		clear();
		for (int i = 0; i < rows; i++) {
			try {
				this.setKeyValue("k-" + i, StringUtils.leftPad("" + i, size));
			} catch (CacheException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
		DCacheDemoService.rows = rows;
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

	public boolean ayncSetKeyValue(String key, String value) {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.add(cacheKey, value);
	}

	public boolean setKeyValue(String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.set(cacheKey, value);
	}

	public String getKeyValue(String key) {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.get(cacheKey);
	}

	public Future<String> asyncGetKeyValueByFuture(String key) throws CacheException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.asyncGet(cacheKey);
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

	public Future<Boolean> asyncSetKeyValueByFuture(String key, String value) throws CacheException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.asyncSet(cacheKey, value);
	}

	public boolean asyncSetKeyValue(String key, String value) throws CacheException, InterruptedException,
			ExecutionException {
		Future<Boolean> future = asyncSetKeyValueByFuture(key, value);
		return future.get();
	}

	public void asyncSetKeyValueByCallback(String key, String value) {
		asyncSetKeyValueByCallback(key, value, true);
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

	@Override
	public long inc(String key, int amount) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.increment(cacheKey, amount);
	}

	@Override
	public long dec(String key, int amount) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.decrement(cacheKey, amount);
	}

	public boolean addKeyValue(String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.addIfAbsent(cacheKey, value);
	}

	@Override
	public long inc(String key, int amount, long def) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.increment(cacheKey, amount, def);
	}

	@Override
	public long dec(String key, int amount, long def) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.decrement(cacheKey, amount, def);
	}

	@Override
	public CASResponse cas(String key, long casId, Object value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.cas(cacheKey, casId, value);
	}

	@Override
	public CASValue gets(String key) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.gets(cacheKey);
	}

	@Override
	public boolean setKeyDoubleValue(String key, Double value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.set(cacheKey, value);
	}

	@Override
	public Double getKeyDoubleValue(String key) {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.get(cacheKey);
	}

	@Override
	public Object getKeyValue(String category, String key) {
		CacheKey cacheKey = new CacheKey(category, key);
		return cacheService.get(cacheKey);
	}

	@Override
	public boolean setKeyValue(String category, String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey(category, key);
		return cacheService.set(cacheKey, value);
	}

	@Override
	public boolean asyncDeleteKey(String key) throws CacheException, InterruptedException, ExecutionException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.asyncDelete(cacheKey).get();
	}

}
