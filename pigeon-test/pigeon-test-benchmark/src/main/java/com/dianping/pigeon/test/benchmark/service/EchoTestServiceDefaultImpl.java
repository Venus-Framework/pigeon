package com.dianping.pigeon.test.benchmark.service;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dianping.pigeon.remoting.common.codec.kryo.KryoSerializer;
import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;
import com.dianping.pigeon.remoting.invoker.util.InvokerHelper;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service
public class EchoTestServiceDefaultImpl implements EchoTestService {

	@Reference(url = "com.dianping.pigeon.demo.EchoService", serialize = "kryo")
	EchoService echoService;
	{
		KryoSerializer.registerClass(Date.class, 100);
		KryoSerializer.registerClass(EchoService.class, 101);
	}

	static EchoTestServiceDefaultImpl instance = new EchoTestServiceDefaultImpl();

	public void concurrentGetNow(final int threads, final int timeout) {
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					getNow(timeout);
				}
			});
		}
	}

	private void getNow(final int timeout) {
		InvokerHelper.setTimeout(timeout);
		while (true) {
			try {
				echoService.now();
			} catch (RuntimeException e) {
			}
		}
	}

	public static void main(String[] args) {
		instance.getNow(500);
	}
}
