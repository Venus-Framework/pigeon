package com.dianping.pigeon.test.benchmark.service;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.iphub.IpClient;
import com.dianping.iphub.IpInfo;
import com.dianping.iphub.exception.IpHubException;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service(url = "com.dianping.pigeon.test.benchmark.service.IpServiceDefaultImpl")
public class IpServiceDefaultImpl implements IpService {

	public IpInfo getIpInfo(String ip) throws IpHubException {
		return IpClient.getIpInfo(ip);
	}

	protected static Random random = new Random();
	protected static int rows = 0;
	volatile boolean isCancel = false;
	ExecutorService executor = null;

	public void cancel() {
		this.isCancel = true;
		if (executor != null) {
			executor.shutdown();
		}
	}

	public void concurrentGet(final int threads, final int sleepTime) {
		executor = Executors.newFixedThreadPool(threads);
		isCancel = false;
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (!isCancel) {
						Transaction t = Cat.newTransaction("iphub", "getIpInfo");
						for (int i = 0; i < 500; i++) {
							String ip = random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255)
									+ "." + random.nextInt(255);
							try {
								getIpInfo(ip);
								if (sleepTime > 0) {
									Thread.sleep(sleepTime);
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
							}
						}
						t.setStatus(Message.SUCCESS);
						t.complete();
					}
				}
			});
		}
	}
}
