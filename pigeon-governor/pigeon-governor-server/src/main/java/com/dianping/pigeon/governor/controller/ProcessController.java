package com.dianping.pigeon.governor.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.pigeon.governor.bean.ProcessBean;
import com.dianping.pigeon.governor.service.ProcessService;

public class ProcessController extends BaseController {

	private Logger log = LogManager.getLogger();
	
	@Autowired
	private ProcessService processService;
	
	@RequestMapping(value = {"/process"}, method = RequestMethod.POST)
	@ResponseBody
	public String process(ModelMap modelMap,
							ProcessBean processBean,
							HttpServletRequest request,
							HttpServletResponse response) {
		
		return null;
	}
}
