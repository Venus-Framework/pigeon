package com.dianping.pigeon.governor.service;

import java.util.List;
import java.util.concurrent.TimeoutException;

import com.dianping.cache.core.CASResponse;
import com.dianping.cache.core.CASValue;
import com.dianping.cache.exception.CacheException;

public interface CacheDemoService {

	void concurrentGet(final int threads, final int rows);

	void concurrentSet(int threads, final int rows, final int size);

	void cancel();

	void init(int rows, int size);

	void clear();

	public boolean ayncSetKeyValue(String key, String value);

	public boolean setKeyValue(String key, String value) throws CacheException, TimeoutException;

	public boolean setKeyDoubleValue(String key, Double value) throws CacheException, TimeoutException;

	public Double getKeyDoubleValue(String key);

	public String getKeyValue(String key);

	public List<Object> bulkGetKeyValue(String keys);

	public boolean removeKey(String key);

	public long inc(String key, int amount) throws CacheException, TimeoutException;

	public long dec(String key, int amount) throws CacheException, TimeoutException;

	public boolean addKeyValue(String key, String value) throws CacheException, TimeoutException;

	public long inc(String key, int amount, long def) throws CacheException, TimeoutException;

	public long dec(String key, int amount, long def) throws CacheException, TimeoutException;

	public CASResponse cas(String key, long casId, Object value) throws CacheException, TimeoutException;

	public CASValue gets(String key) throws CacheException, TimeoutException;

}
