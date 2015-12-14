package com.dianping.pigeon.governor.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.pigeon.governor.dao.ProjectMapper;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.ServiceService;
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

	private Gson gson = new Gson();
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserService userService;
	@Autowired
	private ProjectOwnerService projectOwnerService;
	@Autowired
	private ServiceService serviceService;

	/**
	 * 首页：查询缓存中的所有应用，跳转到应用服务配置页
	 * @param modelMap
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = {"/","/index"}, method = RequestMethod.GET)
	public String index(ModelMap modelMap,
								   HttpServletRequest request,
								   HttpServletResponse response) {
		commonnav(modelMap, request);
		//List<String> projectNames = projectService.retrieveAllNameByCache();
		List<Project> projects = projectService.retrieveAllIdNamesByCache();
		List<Service> services = serviceService.retrieveAllIdNamesByCache();

		//modelMap.addAttribute("projectNames",gson.toJson(projectNames));
		modelMap.addAttribute("projects", gson.toJson(projects));
		modelMap.addAttribute("services", gson.toJson(services));

		return "/index";
	}

	/**
	 * 显示我的应用
	 * @param modelMap
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = {"/projects"}, method = RequestMethod.GET)
	public String allinone(ModelMap modelMap,
						   HttpServletRequest request, HttpServletResponse response) {
		User user = (User) request.getSession().getAttribute(Constants.DP_USER);
		String dpAccount = user!=null ? user.getDpaccount() : "";
		modelMap.addAttribute("currentUser", dpAccount);
		modelMap.addAttribute("projectOwner", dpAccount);

		return "/projects/mine";
	}

	/**
	 * 显示所有应用
	 * @param modelMap
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = {"/projects.all"}, method = RequestMethod.GET)
	public String projectOwnerInfo(ModelMap modelMap,
									HttpServletRequest request,
									HttpServletResponse response) {
		commonnav(modelMap, request);

		return "/projects/all";
	}

	/**
	 * jqgrid表格插件根据应用拥有者（默认为当前登录人）查询应用列表
	 * @param modelMap
	 * @param jqGridReqBean
	 * @param projectOwner
	 * @param request
	 * @param response
	 * @return
	 */
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

	/**
	 * jqgrid应用编辑接口
	 * @param modelMap
	 * @param projectBean
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = {"/projects.api"}, method = RequestMethod.POST)
	public void allinoneapi(ModelMap modelMap, ProjectBean projectBean,
			HttpServletRequest request, HttpServletResponse response) {
		String oper = projectBean.getOper();
		
		try {
			verifyIdentity(request, 2);
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

	/**
	 * jqgrid表格插件查询所有应用列表
	 * @param modelMap
	 * @param jqGridReqBean
	 * @param request
	 * @param response
	 * @return
	 */
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
