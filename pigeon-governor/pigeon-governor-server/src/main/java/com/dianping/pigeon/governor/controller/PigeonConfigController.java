package com.dianping.pigeon.governor.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.pigeon.governor.bean.ServiceBean;
import com.dianping.pigeon.governor.bean.WebResult;
import com.dianping.pigeon.governor.dao.ServiceMapper;
import com.dianping.pigeon.governor.model.Service;

@Controller
public class PigeonConfigController {
	
	private Logger log = LogManager.getLogger();
	
	@Autowired
	private ServiceMapper serviceMapper;
	
	@RequestMapping(value = {"/services"}, method = RequestMethod.GET)
	public String index(ModelMap modelMap,
			HttpServletRequest request, HttpServletResponse response) {
		
		log.info("into list services ...");
		
		List<Service> services = serviceMapper.selectByExample(null);
		modelMap.addAttribute("services", services);
		
		return "/services/index";
	}

	@RequestMapping(value = {"/services/{id}"}, method = RequestMethod.GET)
	public String show(ModelMap modelMap, @PathVariable Integer id,
			HttpServletRequest request, HttpServletResponse response) {
		
		modelMap.addAttribute("service", serviceMapper.selectByPrimaryKey(id));
		
		return "/services/show";
	}
	
	@RequestMapping(value = {"/services/new"}, method = RequestMethod.GET)
	public String newPage(ModelMap modelMap,
			HttpServletRequest request, HttpServletResponse response) {
		
		return "/services/new";
	}
	
	@RequestMapping(value = {"/services/{id}/edit"}, method = RequestMethod.GET)
	public String edit(ModelMap modelMap, @PathVariable Integer id,
			HttpServletRequest request, HttpServletResponse response) {
		
		modelMap.addAttribute("service", serviceMapper.selectByPrimaryKey(id));
		
		return "/services/edit";
	}
	
	@RequestMapping(value = {"/services"}, method = RequestMethod.POST)
	@ResponseBody
	public WebResult create(ModelMap modelMap, ServiceBean serviceBean,
			HttpServletRequest request, HttpServletResponse response) {
		
		WebResult result = new WebResult(request);
		
		Service service = serviceBean.createService();
		
		int sqlResult = -1;
		
		if(StringUtils.isBlank(service.getName()) || service.getProjectid() == null || service.getEnvid() == null){
			sqlResult = -1;
		} else {
			sqlResult = serviceMapper.insertSelective(service);
		}
		
		if(sqlResult > 0){
			result.setStatus(200);
			result.setMessage("Insert service successfully...");
		}else{
			result.setHasError(true);
			result.setErrorMsg("Failed to insert service...");
		}
		
		return result;
	}
	
	@RequestMapping(value = {"/services/{id}"}, method = {RequestMethod.POST, RequestMethod.PUT})
	@ResponseBody
	public WebResult update(ModelMap modelMap,
			ServiceBean serviceBean, @PathVariable Integer id,
			HttpServletRequest request, HttpServletResponse response) {
		
		WebResult result = new WebResult(request);
		Service service = serviceBean.convertToService();
		
		int sqlResult = -1;
		
		if(StringUtils.isBlank(service.getName()) || service.getProjectid() == null || service.getEnvid() == null){
			sqlResult = -1;
		} else {
			sqlResult = serviceMapper.updateByPrimaryKeySelective(service);
		}
		
		if(sqlResult > 0){
			result.setStatus(200);
			result.setMessage("Update service successfully...");
		}else{
			result.setHasError(true);
			result.setErrorMsg("Failed to update service...");
		}
		
		return result;
	}
	
	@RequestMapping(value = {"/services/{id}/delete"}, method = {RequestMethod.GET, RequestMethod.DELETE})
	@ResponseBody
	public WebResult destroy(ModelMap modelMap, @PathVariable Integer id,
			HttpServletRequest request, HttpServletResponse response) {
		
		WebResult result = new WebResult(request);
		
		// TODO 加入一些权限之类的判断条件
		int sqlResult = -1;
		if(authenticate()){
			sqlResult = serviceMapper.deleteByPrimaryKey(id);
		}
		
		if(sqlResult > 0){
			result.setStatus(200);
			result.setMessage("Destroy service successfully...");
		}else{
			result.setStatus(500);
			result.setHasError(true);
			result.setErrorMsg("Failed to destroy service...");
		}
		
		return result;
	}
	
	/**
	 * 敏感操作权限控制
	 * @return
	 */
	public boolean authenticate(){
		
		return true;
	}
}
