package com.dianping.pigeon.test.benchmark.cache;

import java.util.List;
import java.util.concurrent.TimeoutException;

import com.dianping.cache.exception.CacheException;

public interface CacheTestService {

	public void init(int rows);

	public void clear();

	public boolean setKeyValue(String key, String value);

	public String getKeyValue(String key);

	public List<Object> bulkGetKeyValue(String keys);

	public boolean removeKey(String key);

	public boolean addKeyValue(String key, String value) throws CacheException, TimeoutException;

	public void concurrentGet(int threads);
}
