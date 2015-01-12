package com.dianping.pigeon.test.benchmark.message;

public interface MessageTestService {

	void init(int rows) throws Exception;

	void clear() throws Exception;

	boolean setKeyValue(String key, String value) throws Exception;

	String getKeyValue(String key) throws Exception;

	boolean removeKey(String key) throws Exception;

	void concurrentGet(final int threads, final int rows);

	void concurrentSet(int threads, final int rows, final int size, int sleepTime);

	void cancel();
}
