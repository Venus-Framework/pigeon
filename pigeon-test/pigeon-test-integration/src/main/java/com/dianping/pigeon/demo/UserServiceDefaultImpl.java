/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import java.util.ArrayList;
import java.util.List;


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

}
