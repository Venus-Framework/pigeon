/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.benchmark.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.pigeon.test.benchmark.domain.PhoneCard;

@Controller
public class PhoneBookController {

	@RequestMapping("/list")
	public String gotoAllPage() {
		return "index";
	}

	@RequestMapping("/new")
	public String gotoNewPage() {
		return "new";
	}

	@RequestMapping("/findPhoneCardByName/{name}")
	public @ResponseBody
	List<PhoneCard> findPhoneCardByName(@PathVariable String name) {
		return null;
	}

	@RequestMapping("/getAllPhoneCards")
	public @ResponseBody
	List<PhoneCard> getAllPhoneCards() {
		return null;
	}

	@RequestMapping(value = "addPhoneCard", method = RequestMethod.POST)
	public String addPhoneCard(@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "mobile", required = true) String mobile) {
		return "redirect:/";
	}

	@RequestMapping("/deletePhoneCardById")
	public @ResponseBody
	Object deletePhoneCardById(@RequestParam(value = "id", required = true) int id) {
		return "success";
	}

	@RequestMapping("/updatePhoneCardRandomly")
	public @ResponseBody
	Object updatePhoneCardRandomly(@RequestParam(value = "rows", required = true) int rows) {
		return "success";
	}

	@RequestMapping("/getPhoneCardRandomly")
	public @ResponseBody
	Object getPhoneCardRandomly(@RequestParam(value = "rows", required = true) int rows) {
		return "success";
	}
}
