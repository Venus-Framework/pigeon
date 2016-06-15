package com.dianping.pigeon.governor.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.lion.Environment;
import com.dianping.pigeon.governor.model.User;
import org.springframework.ui.ModelMap;

import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.IPUtils;

/**
 * 
 * @author chenchongze
 *
 */
public class BaseController {
	public User getUserInfo(HttpServletRequest request){
		User user = (User)request.getSession().getAttribute(Constants.DP_USER);
		return user;
	}
	public void commonnav(Map<String, Object> map, HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute(Constants.DP_USER);
		String currentUser = user!=null?user.getDpaccount():"";
		String userName = user!=null?user.getUsername():"";
		map.put("currentUser", currentUser);
		map.put("userName",userName);
	}

	public void commonnav(ModelMap modelMap, HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute(Constants.DP_USER);
		String currentUser = user!=null?user.getDpaccount():"";
		String userName = user!=null?user.getUsername():"";
		modelMap.addAttribute("currentUser", currentUser);
		modelMap.addAttribute("userName",userName);
	}

	public void verifyIdentity(int agentId) throws Exception{

		if(3 != agentId){
			throw new Exception(String.format("agentId %d is not system level", agentId));
		}

	}

	public void verifyIdentity(HttpServletRequest request, int agentId) throws Exception{

		String ip = IPUtils.getUserIP(request);
		if(!"0:0:0:0:0:0:0:1%0".equals(ip)){
			throw new Exception("You have no privilege.");
		}

		if(2 != agentId) {
			throw new Exception(String.format("agentId %d is not system level", agentId));
		}

	}
}
