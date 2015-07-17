package com.dianping.pigeon.governor.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.pigeon.governor.service.RegistrationInfoService;
import com.dianping.pigeon.governor.util.GsonUtils;

@Controller
public class ServiceRegitrationInfoQueryController {

	private static final Logger LOG = LogManager.getLogger(ServiceRegitrationInfoQueryController.class);

	@Autowired
	RegistrationInfoService registrationInfoService;

	@RequestMapping(value = { "/" })
	public ModelAndView viewHome(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("path", "service");
		return new ModelAndView("common/main-container", map);
	}

	@RequestMapping(value = { "/service" })
	public ModelAndView viewSerivice(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("path", "service");
		return new ModelAndView("common/main-container", map);
	}

	@RequestMapping(value = { "/service/{url}/{group}" })
	@ResponseBody
	public String queryServiceInfoByGet(@PathVariable String url, @PathVariable String group) {
		Map<String, Object> map = queryServiceInfoByUrlAndGroup(url, group);

		return GsonUtils.toJson(map);
	}

	private Map<String, Object> queryServiceInfoByUrlAndGroup(String url, String group) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			List<String> addressList = null;
			if (StringUtils.isEmpty(group)) {
				addressList = registrationInfoService.getAddressListOfService(url);
			} else {
				addressList = registrationInfoService.getAddressListOfService(url, group);
			}
			String app = registrationInfoService.getAppOfService(url);
			map.put("url", url);
			map.put("address", StringUtils.join(addressList, ","));
			map.put("app", app);
			map.put("group", group);

			// map.put("subPath", "create");
		} catch (Exception e) {
			// @TODO: error page.
		}
		map.put("path", "service");

		return map;
	}

	@RequestMapping(value = { "/service/query" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public ModelAndView queryServiceInfoByPost(String url, String group) {
		Map<String, Object> map = queryServiceInfoByUrlAndGroup(url, group);

		return new ModelAndView("common/main-container", map);
	}
}
