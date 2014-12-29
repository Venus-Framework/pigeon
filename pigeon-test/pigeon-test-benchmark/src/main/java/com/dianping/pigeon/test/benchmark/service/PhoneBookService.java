/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.benchmark.service;

import java.util.List;

import com.dianping.pigeon.test.benchmark.domain.PhoneCard;

public interface PhoneBookService {

	public List<PhoneCard> findPhoneCardByName(String name);

	public List<PhoneCard> getAllPhoneCards();

	public void addPhoneCard(String name, String mobile);

	public void deletePhoneCardById(int id);

	public void updatePhoneCardRandomly(int threads, int rows, final int sleepTime);

	public void updatePhoneCardRandomly(int rows);

	public List<PhoneCard> getPhoneCardRandomly(int rows);

	public void getPhoneCardRandomly(final int threads, final int rows, final int sleepTime);

	public void init(int rows);

	public void clear();
	
	void cancel();
}
