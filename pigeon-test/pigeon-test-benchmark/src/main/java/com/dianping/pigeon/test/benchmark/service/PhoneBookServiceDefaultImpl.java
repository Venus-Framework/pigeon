/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.benchmark.service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.pigeon.test.benchmark.domain.PhoneCard;
import com.dianping.pigeon.test.benchmark.persistence.PhoneBookMapper;

@Service
public class PhoneBookServiceDefaultImpl implements PhoneBookService {

	static int rows;
	static Random random = new Random();

	@Autowired
	private PhoneBookMapper phoneBookMapper;

	public List<PhoneCard> findPhoneCardByName(String name) {
		return phoneBookMapper.findPhoneCardByName(name);
	}

	public List<PhoneCard> getAllPhoneCards() {
		return phoneBookMapper.getAllPhoneCards();
	}

	public void addPhoneCard(String name, String mobile) {
		PhoneCard card = new PhoneCard();
		card.setName(name);
		card.setMobile(mobile);
		phoneBookMapper.insertPhoneCard(card);
	}

	public void deletePhoneCardById(int id) {
		phoneBookMapper.deletePhoneCard(id);
	}

	public void updatePhoneCardRandomly(final int threads, final int rows) {
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (true) {
						updatePhoneCardRandomly(rows);
					}
				}
			});

		}
	}

	public void updatePhoneCardRandomly(final int rows) {
		PhoneCard card = new PhoneCard();
		String name = "" + Math.abs((int) (random.nextDouble() * rows));
		card.setMobile("138" + name);
		card.setName(name);

		phoneBookMapper.updatePhoneCard(card);
	}

	public void getPhoneCardRandomly(final int threads, final int rows) {
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (true) {
						getPhoneCardRandomly(rows);
					}
				}
			});

		}
	}

	public List<PhoneCard> getPhoneCardRandomly(int rows) {
		String name = "" + Math.abs((int) (random.nextDouble() * rows));
		return phoneBookMapper.findPhoneCardByName(name);
	}

	public void init(int rows) {
		clear();
		for (int i = 0; i < rows; i++) {
			addPhoneCard("" + i, "139" + i);
		}
		PhoneBookServiceDefaultImpl.rows = rows;
	}

	public void clear() {
		phoneBookMapper.clear();
	}
}
