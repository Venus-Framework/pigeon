package com.dianping.pigeon.governor.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DocumentController {

	private Logger log = LogManager.getLogger(this.getClass());
	
	@RequestMapping(value = { "/help" })
	public ModelAndView viewHelp(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("path", "help");
		return new ModelAndView("main/help/main", map);
	}
}
