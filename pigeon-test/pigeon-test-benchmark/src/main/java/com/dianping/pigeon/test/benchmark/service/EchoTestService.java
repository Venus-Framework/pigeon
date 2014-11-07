package com.dianping.pigeon.test.benchmark.service;


public interface EchoTestService {

	public void concurrentGetNow(final int threads, final int timeout);

}
