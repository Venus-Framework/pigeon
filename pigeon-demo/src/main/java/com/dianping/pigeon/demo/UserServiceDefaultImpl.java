/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import java.util.ArrayList;
import java.util.List;

import com.dianping.pigeon.remoting.provider.util.ProviderHelper;
import com.dianping.pigeon.util.ContextUtils;

public class UserServiceDefaultImpl implements UserService {

	@Override
	public User[] getUserDetail(User[] users, boolean withPassword) {
		// System.out.println("received: " + users);
		List<Role> roles = new ArrayList<Role>();
		roles.add(new Role("a"));
		roles.add(new Role("b"));
		for (User user : users) {
			user.setEmail(user.getUsername() + "@dianping.com");
			if (withPassword) {
				user.setPassword("123456");
			}
			user.setRoles(roles);
			if (user.getUsername() != null && user.getUsername().equals("scott")) {
				throw new RuntimeException("invalid user!");
			}
		}
		return users;
	}

	@Override
	public String echo(String msg) {
		// System.out.println("request-key:" +
		// ContextUtils.getLocalContext("key1"));
		// System.out.println("global-SOURCE_APP:" +
		// ContextUtils.getGlobalContext("SOURCE_APP"));
		// System.out.println("SOURCE_APP:" +
		// ContextUtils.getGlobalContext("SOURCE_APP"));
		// System.out.println("SOURCE_IP:" +
		// ContextUtils.getGlobalContext("SOURCE_IP"));
		//System.out.println(msg);
//		try {
//			Thread.sleep(30);
//		} catch (InterruptedException e) {
//		}
		ProviderHelper.writeSuccessResponse(ProviderHelper.getContext(), "user service:" + msg);
		return msg;
	}

}
