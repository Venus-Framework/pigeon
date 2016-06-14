/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.annotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dianping.pigeon.demo.DealGroupBaseDTO;
import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.google.common.collect.Lists;

@Service
public class EchoServiceAnnotationImpl implements EchoService {

	List<User<?>> users = new ArrayList<User<?>>();

	public EchoServiceAnnotationImpl() {
	}

	@Override
	public String echo(String input) {
		return "echo:" + input;
	}
	
	@Override
	public String echo2(Integer size) throws IOException {
		//throw new RuntimeException("error with echo service");
		try {
			Thread.sleep(size);
		} catch (InterruptedException e) {
		}
		return "echo:" + size;
	}

	@Override
	public long now() {
		return System.currentTimeMillis();
	}

	@Override
	public List<User<?>> findUsers(int count) {
		return Lists.newArrayList(users.subList(0, count));
	}

	@Override
	public void addUser(User<?> user) {
		users.add(user);
	}

	@Override
	public String test(Map<User, String> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String asyncEcho(String msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> testMap(Map<String, String> values) {
		return values;
	}

	@Override
	public DealGroupBaseDTO test(DealGroupBaseDTO dto) {
		return dto;
	}

	@Override
	public String echo(List<Gender> genders) {
		for(Gender g : genders) {
			System.out.println(g);
		}
		return genders.toString();
	}
}
