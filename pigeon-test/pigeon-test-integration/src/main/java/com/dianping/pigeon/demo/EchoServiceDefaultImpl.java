/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import java.util.ArrayList;
import java.util.List;

import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.provider.util.ProviderHelper;
import com.dianping.pigeon.util.ContextUtils;

public class EchoServiceDefaultImpl implements EchoService {

	List<User<?>> users = new ArrayList<User<?>>();

	UserService userService = ServiceFactory.getService(UserService.class, 1000);

	public EchoServiceDefaultImpl() {
	}

	@Override
	public String echo(String input) {
		System.out.println("key:" + ContextUtils.getContextValue("key1"));
		System.out.println("SOURCE_APP:" + ContextUtils.getContextValue("SOURCE_APP"));
		System.out.println("SOURCE_IP:" + ContextUtils.getContextValue("SOURCE_IP"));
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
		}
		return "echo:" + userService.echo(input);
		//ProviderHelper.writeSuccessResponse(ProviderHelper.getContext(), "echo#" + input);
		//return "echo:" + input;
	}

	@Override
	public long now() {
		return System.currentTimeMillis();
	}

	@Override
	public List<User<?>> findUsers(int count) {
		// return Lists.newArrayList(users.subList(0, count));
		return users.subList(0, count);
	}

	@Override
	public void addUser(User<?> user) {
		users.add(user);
	}

}
