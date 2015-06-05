/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import java.util.ArrayList;
import java.util.List;

import com.dianping.pigeon.util.ContextUtils;

public class EchoServiceDefaultImpl implements EchoService {

	List<User> users = new ArrayList<User>();

	public EchoServiceDefaultImpl() {
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
		// return Lists.newArrayList(users.subList(0, count));
		return users.subList(0, count);
	}

	@Override
	public void addUser(User user) {
		users.add(user);
	}

}
