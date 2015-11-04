package com.dianping.pigeon.governor.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.pigeon.governor.dao.ProjectMapper;
import com.dianping.pigeon.governor.model.Project;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.pigeon.governor.bean.JqGridReqBean;
import com.dianping.pigeon.governor.bean.JqGridReqFilters;
import com.dianping.pigeon.governor.bean.JqGridRespBean;
import com.dianping.pigeon.governor.bean.ProjectBean;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.UserService;
import com.dianping.pigeon.governor.util.Constants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author chenchongze
 *
 */
@Controller
public class ProjectController extends BaseController {

	private Logger logger = LogManager.getLogger();
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserService userService;
	@Autowired
	private ProjectOwnerService projectOwnerService;

	@RequestMapping(value = {"/"}, method = RequestMethod.GET)
	public String index(ModelMap modelMap,
								   HttpServletRequest request,
								   HttpServletResponse response) {
		String currentUser = (String) request.getSession().getAttribute(Constants.DP_ACCOUNT);
		modelMap.addAttribute("currentUser", currentUser);
		//List<String> projectNames = projectService.retrieveAllNameByCache();
		List<Project> projects = projectService.retrieveAllByCache();

		//modelMap.addAttribute("projectNames",new Gson().toJson(projectNames));
		modelMap.addAttribute("projects",projects);

		return "/index";
	}
	
	@RequestMapping(value = {"/projects.all"}, method = RequestMethod.GET)
	public String projectOwnerInfo(ModelMap modelMap,
									HttpServletRequest request,
									HttpServletResponse response) {
		String currentUser = (String) request.getSession().getAttribute(Constants.DP_ACCOUNT);
		modelMap.addAttribute("currentUser", currentUser);

		return "/projects/list";
	}
	
	@RequestMapping(value = {"/projects"}, method = RequestMethod.POST)
	@ResponseBody
	public JqGridRespBean projectsRetrieve(ModelMap modelMap,
											JqGridReqBean jqGridReqBean,
											@RequestParam(value="projectOwner") String projectOwner,
											HttpServletRequest request,
											HttpServletResponse response) {
		JqGridRespBean jqGridTableBean;
		
		int page = jqGridReqBean.getPage();
		int rows = jqGridReqBean.getRows();
		
		if(page > 0 && rows > 0){
			jqGridTableBean = projectService.retrieveByJqGrid(page, rows, projectOwner);
		}else{
			jqGridTableBean = projectService.retrieveByJqGrid(1, 10, projectOwner);
		}
		
		return jqGridTableBean;
		
	}
	
	@RequestMapping(value = {"/projects"}, method = RequestMethod.GET)
	public String allinone(ModelMap modelMap,
			HttpServletRequest request, HttpServletResponse response) {
		String currentUser = (String) request.getSession().getAttribute(Constants.DP_ACCOUNT);
		modelMap.addAttribute("currentUser", currentUser);
		modelMap.addAttribute("projectOwner", currentUser);
		
		return "/projects/index";
	}
	
	@RequestMapping(value = {"/projects.api"}, method = RequestMethod.POST)
	public void allinoneapi(ModelMap modelMap, ProjectBean projectBean,
			HttpServletRequest request, HttpServletResponse response) {
		String oper = projectBean.getOper();
		
		try {
			verifyIdentity(request, 3);
		} catch (Exception e) {
			
			e.printStackTrace();
			return ;
		}
		
		if("edit".equals(oper)){
			projectService.updateById(projectBean);
			
		}else if("del".equals(oper)){
			projectService.deleteByIdSplitByComma(projectBean.getId());
		
		}else if("add".equals(oper)){
			projectService.create(projectBean);
		
		}
	}
	
	@RequestMapping(value = {"/projects.json"}, method = RequestMethod.POST)
	@ResponseBody
	public JqGridRespBean allinonejson(ModelMap modelMap, JqGridReqBean jqGridReqBean,
			HttpServletRequest request, HttpServletResponse response) {
		
		JqGridReqFilters filters = null;
		
		if(StringUtils.isNotBlank(jqGridReqBean.getFilters())){
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				filters = objectMapper.readValue(jqGridReqBean.getFilters(), JqGridReqFilters.class);
			} catch (JsonParseException e) {
				logger.error("JsonParse", e);
			} catch (JsonMappingException e) {
				logger.error("JsonMapping", e);
			} catch (IOException e) {
				logger.error("IO", e);
			}
		}

		//TODO 根据filter查询
		
		JqGridRespBean jqGridTableBean;
		
		int page = jqGridReqBean.getPage();
		int rows = jqGridReqBean.getRows();
		
		if(page > 0 && rows > 0){
			jqGridTableBean = projectService.retrieveByJqGrid(page, rows);
		}else{
			jqGridTableBean = projectService.retrieveByJqGrid(1, 10);
		}
		
		return jqGridTableBean;
		
	}
}
