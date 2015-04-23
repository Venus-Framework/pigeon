package com.dianping.pigeon.governor.service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.dianping.cache.core.CASResponse;
import com.dianping.cache.core.CASValue;
import com.dianping.cache.exception.CacheException;

public interface CacheDemoService {

	void concurrentGet(final int threads, final int rows);

	void concurrentSet(int threads, final int rows, final int size);

	void cancel();

	void init(int rows, int size) throws CacheException, TimeoutException;

	void clear() throws CacheException, TimeoutException;

	public boolean ayncSetKeyValue(String key, String value);

	public boolean setKeyValue(String key, String value) throws CacheException, TimeoutException;

	public boolean setKeyDoubleValue(String key, Double value) throws CacheException, TimeoutException;

	public Double getKeyDoubleValue(String key);

	public String getKeyValue(String key);

	public List<Object> bulkGetKeyValue(String keys);

	public boolean deleteKey(String key) throws CacheException, TimeoutException;

	public boolean asyncDeleteKey(String key) throws CacheException, InterruptedException, ExecutionException;

	public long inc(String key, int amount) throws CacheException, TimeoutException;

	public long dec(String key, int amount) throws CacheException, TimeoutException;

	public boolean addKeyValue(String key, String value) throws CacheException, TimeoutException;

	public long inc(String key, int amount, long def) throws CacheException, TimeoutException;

	public long dec(String key, int amount, long def) throws CacheException, TimeoutException;

	public CASResponse cas(String key, long casId, Object value) throws CacheException, TimeoutException;

	public CASValue gets(String key) throws CacheException, TimeoutException;

	public Object getKeyValue(String category, String key);

	public boolean setKeyValue(String category, String key, String value) throws CacheException, TimeoutException;

	public Future<String> asyncGetKeyValueByFuture(String key) throws CacheException;

	public Future<Boolean> asyncSetKeyValueByFuture(String key, String value) throws CacheException;

	public boolean asyncSetKeyValue(String key, String value) throws CacheException, InterruptedException,
			ExecutionException;

	public String asyncGetKeyValue(String key) throws Exception;

	public void concurrentAsyncGet(int threads, final int rows);

	public void concurrentAsyncSet(int threads, final int rows, final int size);
	
	public void asyncGetKeyValueByCallback(String key);
	
	public void asyncSetKeyValueByCallback(String key, String value);
}
