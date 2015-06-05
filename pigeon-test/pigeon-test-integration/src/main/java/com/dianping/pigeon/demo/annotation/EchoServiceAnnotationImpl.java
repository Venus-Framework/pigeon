/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.annotation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dianping.pigeon.demo.EchoService;
import com.google.common.collect.Lists;

@Service
public class EchoServiceAnnotationImpl implements EchoService {

	List<User> users = new ArrayList<User>();

	public EchoServiceAnnotationImpl() {
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
	}

	@Override
	public void addUser(User user) {
		users.add(user);
	}
}
