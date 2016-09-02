package com.dianping.pigeon.governor.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.UserService;
import com.dianping.pigeon.governor.util.Constants;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {

	private Logger log = LogManager.getLogger();

	@Autowired
	private UserService userService;

	@RequestMapping(value = {"/","/index"}, method = RequestMethod.GET)
	public String index(ModelMap modelMap,
						HttpServletRequest request, HttpServletResponse response) {
		User user = (User) request.getSession().getAttribute(Constants.DP_USER);

		return "/admin/index";
	}

}
