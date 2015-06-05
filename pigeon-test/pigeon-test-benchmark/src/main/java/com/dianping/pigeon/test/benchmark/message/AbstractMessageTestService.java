package com.dianping.pigeon.test.benchmark.message;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public abstract class AbstractMessageTestService implements MessageTestService {

	protected static Random random = new Random();
	protected static int rows = 0;
	volatile boolean isCancel = false;
	ExecutorService executor = null;
	private static Logger logger = LoggerLoader.getLogger(AbstractMessageTestService.class);

	public void concurrentGet(int threads, final int rows) {
		executor = Executors.newFixedThreadPool(threads);
		isCancel = false;
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (!isCancel) {
						Transaction t = Cat.newTransaction("msg", "msg");
						for (int i = 0; i < 500; i++) {
							try {
								getKeyValue("k-" + Math.abs((int) (random.nextDouble() * rows)));
							} catch (Exception e) {
								logger.error("", e);
							}
						}
						t.setStatus(Message.SUCCESS);
						t.complete();
					}
				}
			});
		}
	}

	public void concurrentSet(int threads, final int rows, final int size, final int sleepTime) {
		executor = Executors.newFixedThreadPool(threads);
		isCancel = false;
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (!isCancel) {
						Transaction t = Cat.newTransaction("msg", "msg");
						for (int i = 0; i < 500; i++) {
							try {
								Thread.sleep(sleepTime);
								setKeyValue("k-" + Math.abs((int) (random.nextDouble() * rows)), StringUtils.leftPad("" + i, size));
							} catch (Exception e) {
								logger.error("", e);
							}
						}
						t.setStatus(Message.SUCCESS);
						t.complete();
					}
				}
			});
		}
	}

	@Override
	public void init(int rows) throws Exception {
		clear();
		for (int i = 0; i < rows; i++) {
			this.setKeyValue("k-" + i, i + "");
		}
		AbstractMessageTestService.rows = rows;
	}

	@Override
	public void clear() throws Exception {
		for (int i = 0; i < rows; i++) {
			this.removeKey("k-" + i);
		}
	}

	@Override
	public void cancel() {
		this.isCancel = true;
		if (executor != null) {
			executor.shutdown();
		}
	}
}
