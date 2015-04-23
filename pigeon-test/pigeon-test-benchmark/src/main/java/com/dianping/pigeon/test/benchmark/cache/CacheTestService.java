package com.dianping.pigeon.test.benchmark.cache;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.dianping.cache.exception.CacheException;

interface CacheTestService {

	void init(int rows, int size) throws CacheException, TimeoutException;

	void clear() throws CacheException, TimeoutException;
	
	public boolean deleteKey(String key) throws CacheException, TimeoutException;

	public boolean asyncDeleteKey(String key) throws CacheException, InterruptedException, ExecutionException;

	boolean setKeyValue(String key, String value);

	String getKeyValue(String key);

	List<Object> bulkGetKeyValue(String keys);

	boolean addKeyValue(String key, String value) throws CacheException, TimeoutException;

	void concurrentGet(final int threads, final int rows);

	void concurrentSet(int threads, final int rows, final int size);
	
	void cancel();
	
	public Future<String> asyncGetKeyValueByFuture(String key) throws CacheException;

	public Future<Boolean> asyncSetKeyValueByFuture(String key, String value) throws CacheException;

	public boolean asyncSetKeyValue(String key, String value) throws CacheException, InterruptedException,
			ExecutionException;

	public String asyncGetKeyValue(String key) throws Exception;

	public void concurrentAsyncGet(int threads, final int rows);

	public void concurrentAsyncSet(int threads, final int rows, final int size);
	
	public void asyncGetKeyValueByCallback(String key);
}
