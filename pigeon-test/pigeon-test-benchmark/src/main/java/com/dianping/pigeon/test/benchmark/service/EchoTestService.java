package com.dianping.pigeon.test.benchmark.service;

public interface EchoTestService {

	void concurrentFindUsers(final int threads, final int count, final int timeout);

	void concurrentGetNow(final int threads, final int timeout);

	void cancel();
}
