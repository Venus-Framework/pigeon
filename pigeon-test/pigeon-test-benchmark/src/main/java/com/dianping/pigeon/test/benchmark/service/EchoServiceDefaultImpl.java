/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.benchmark.service;

import java.util.ArrayList;
import java.util.List;

import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.google.common.collect.Lists;

@Service(url = "com.dianping.pigeon.demo.EchoService")
public class EchoServiceDefaultImpl implements EchoService {

	List<User> users = new ArrayList<User>();

	public EchoServiceDefaultImpl() {
		//KryoSerializer.registerClass(EchoService.class, 10000);
		//KryoSerializer.registerClass(User.class, 10001);
		for (int i = 1; i <= 10000; i++) {
			String n = "a" + i;
			User u = new User(i, n, n + "@dianping.com", n + "@hongkou district, shanghai", 20);
			users.add(u);
		}
	}

	@Override
	public String echo(String input) {
		return "echo:" + input;
	}

	@Override
	public long now() {
		return System.currentTimeMillis();
	}

	@Override
	public List<User> findUsers(int count) {
		return Lists.newArrayList(users.subList(0, count));
		//return users.subList(0, count);
	}

}
