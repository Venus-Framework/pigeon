package com.dianping.pigeon.test.benchmark.cache;

import java.util.List;
import java.util.concurrent.TimeoutException;

import com.dianping.cache.exception.CacheException;

interface CacheTestService {

	void init(int rows, int size);

	void clear();

	boolean setKeyValue(String key, String value);

	String getKeyValue(String key);

	List<Object> bulkGetKeyValue(String keys);

	boolean removeKey(String key);

	boolean addKeyValue(String key, String value) throws CacheException, TimeoutException;

	void concurrentGet(final int threads, final int rows);

	void concurrentSet(int threads, final int rows, final int size);
	
	void cancel();
}
