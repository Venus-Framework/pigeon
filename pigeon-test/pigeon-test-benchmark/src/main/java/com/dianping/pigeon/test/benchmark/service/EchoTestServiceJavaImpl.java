package com.dianping.pigeon.test.benchmark.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dianping.pigeon.remoting.common.codec.kryo.KryoSerializer;
import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;
import com.dianping.pigeon.remoting.invoker.util.InvokerHelper;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service(url = "com.dianping.pigeon.test.benchmark.service.EchoTestServiceJavaImpl")
public class EchoTestServiceJavaImpl implements EchoTestService {

	@Reference(url = "com.dianping.pigeon.demo.EchoService", serialize = "java")
	EchoService echoService;

	volatile boolean isCancel = false;

	ExecutorService executor = null;

	public EchoTestServiceJavaImpl() {
		KryoSerializer.registerClass(EchoService.class, 1001);
	}

	static EchoTestServiceJavaImpl instance = new EchoTestServiceJavaImpl();

	public void concurrentGetNow(final int threads, final int count) {
		executor = Executors.newFixedThreadPool(threads);
		this.isCancel = false;
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					getNow(count);
				}
			});
		}
	}

	private void getNow(final int count) {
		while (!isCancel) {
			try {
				echoService.findUsers(count);
			} catch (RuntimeException e) {
			}
		}
	}

	public static void main(String[] args) {
		instance.getNow(50);
	}

	@Override
	public void cancel() {
		this.isCancel = true;
	}
}
