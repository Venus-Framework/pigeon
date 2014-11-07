/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.benchmark.persistence;

import java.util.List;

import com.dianping.pigeon.test.benchmark.domain.PhoneCard;

public interface PhoneBookMapper {

	public PhoneCard getPhoneCardById(int id);

	public List<PhoneCard> getAllPhoneCards();

	public List<PhoneCard> findPhoneCardByName(String name);

	public void insertPhoneCard(PhoneCard phoneCard);

	public void deletePhoneCard(int id);

	public void updatePhoneCard(PhoneCard phoneCard);

	public void clear();
}
