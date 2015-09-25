package com.dianping.pigeon.governor.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.pigeon.governor.bean.JqGridReqBean;
import com.dianping.pigeon.governor.bean.JqGridReqFilters;
import com.dianping.pigeon.governor.bean.JqGridRespBean;
import com.dianping.pigeon.governor.bean.ProjectBean;
import com.dianping.pigeon.governor.service.ProjectService;

/**
 * 
 * @author chenchongze
 *
 */
@Controller
public class ProjectController extends BaseController {

	private Logger log = LogManager.getLogger();
	
	@Autowired
	private ProjectService projectService;
	
	@RequestMapping(value = {"/projects"}, method = RequestMethod.GET)
	public String allinone(ModelMap modelMap,
			HttpServletRequest request, HttpServletResponse response) {
		commonnav(modelMap, request);
		
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
			JSONObject jsonObj = JSONObject.fromObject(jqGridReqBean.getFilters());
			filters = (JqGridReqFilters) JSONObject.toBean(jsonObj, JqGridReqFilters.class);
		}
		
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
