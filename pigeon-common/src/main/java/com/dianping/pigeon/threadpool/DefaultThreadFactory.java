/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: DefaultThreadFactory.java, v 0.1 2013-6-29 下午6:06:42
 *          jianhuihuang Exp $
 */
public class DefaultThreadFactory implements ThreadFactory {

	static final AtomicInteger poolNumber = new AtomicInteger(1);
	final AtomicInteger threadNumber;
	final ThreadGroup group;
	final String namePrefix;
	boolean isDaemon = true;

	public DefaultThreadFactory() {
		this("Default-Pool");
	}

	public DefaultThreadFactory(String name) {
		this(name, true);
	}

	public DefaultThreadFactory(String preffix, boolean daemon) {
		this.threadNumber = new AtomicInteger(1);

		this.group = new ThreadGroup(preffix + "-" + poolNumber.getAndIncrement() + "-threadGroup");

		this.namePrefix = preffix + "-" + poolNumber.getAndIncrement() + "-thread-";
		this.isDaemon = daemon;
	}

	/**
	 * TODO, 是否可以设置为daemon？
	 * 
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	public Thread newThread(Runnable r) {
		Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(),
				-3715992351445876736L);

		t.setDaemon(this.isDaemon);
		if (t.getPriority() != 5) {
			t.setPriority(5);
		}

		return t;
	}

	/**
	 * @return the group
	 */
	public ThreadGroup getGroup() {
		return group;
	}

}
