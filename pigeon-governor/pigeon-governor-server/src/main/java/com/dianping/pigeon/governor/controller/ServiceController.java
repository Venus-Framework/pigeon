package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.*;
import com.dianping.pigeon.governor.model.OpLog;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.OpLogService;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.ServiceService;
import com.dianping.pigeon.governor.util.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;

@Controller
public class ServiceController extends BaseController {
	
	private Logger logger = LogManager.getLogger();
	private ExecutorService workThreadPool = ThreadPoolFactory.getWorkThreadPool();

	@Autowired
	private ServiceService serviceService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectOwnerService projectOwnerService;
	@Autowired
	private OpLogService opLogService;

	private Gson gson = new Gson();

	/**
	 * 显示 projectName 的服务配置页面
	 * @param modelMap
	 * @param projectName
	 * @param request
	 * @return
	 */
	@RequestMapping(value = {"/services/{projectName}"}, method = RequestMethod.GET)
	public String projectInfo(ModelMap modelMap,
								@PathVariable final String projectName,
								HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute(Constants.DP_USER);
		String currentUser = user.getDpaccount();
		modelMap.addAttribute("currentUser", currentUser);
		Project project = projectService.findProject(projectName);
		
		if(project == null){
			project = projectService.createProjectFromCmdbOrNot(projectName);

			if(project == null){
				modelMap.addAttribute("errorMsg", "cmdb找不到项目：" + projectName);
				return "/error/500";
			}

			final String emails = project.getEmail();
			ThreadPoolFactory.getWorkThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					//create default project owner
					//TODO product from workflow
					projectOwnerService.createDefaultOwner(emails, projectName);
				}
			});

		}

		modelMap.addAttribute("isProjectOwner",
				UserRole.USER_SCM.getValue().equals(user.getRoleid()) ||
						projectOwnerService.isProjectOwner(currentUser, project));
		modelMap.addAttribute("projectName", projectName);
		modelMap.addAttribute("projectId", project.getId());

		modelMap.addAttribute("services",gson.toJson(serviceService.retrieveAllByProjectName(projectName)));
		
		return "services/list4project";
	}

	/**
	 * 查询 projectName 的服务配置接口
	 * @param jqGridReqBean
	 * @param projectName
	 * @return
	 */
	@RequestMapping(value = {"/services/{projectName}"}, method = RequestMethod.POST)
	@ResponseBody
	public JqGridRespBean servicesRetrieve(JqGridReqBean jqGridReqBean,
											@PathVariable String projectName) {
		
		/*JqGridReqFilters filters = null;
		
		if(StringUtils.isNotBlank(jqGridReqBean.getFilters())){
			
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				filters = objectMapper.readValue(jqGridReqBean.getFilters(), JqGridReqFilters.class);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		JqGridRespBean jqGridTableBean;

		/*int page = jqGridReqBean.getPage();
		int rows = jqGridReqBean.getRows();
		
		if(page > 0 && rows > 0){
			jqGridTableBean = serviceService.retrieveByJqGrid(page, rows, projectName);
		}else{
			jqGridTableBean = serviceService.retrieveByJqGrid(1, 10, projectName);
		}*/

		jqGridTableBean = serviceService.retrieveByJqGrid(projectName);
		
		return jqGridTableBean;
	}

	/**
	 * 根据 应用 编辑服务接口
	 * @param serviceBean
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = {"/services.api/{projectId}"}, method = RequestMethod.POST)
	@ResponseBody
	public Result servicesapi4project(ServiceBean serviceBean,
									@PathVariable Integer projectId,
									HttpServletRequest request,
									HttpServletResponse response) {//设置为void的时候要设置response参数
		String oper = serviceBean.getOper();
		serviceBean.setProjectid(projectId);
		String ipPorts = serviceBean.getHosts();

		if(StringUtils.isNotBlank(ipPorts)) {
			serviceBean.setHosts(IPUtils.getValidHosts(ipPorts));
		}

		Result result = null;

		try {
			if("edit".equals(oper)){
				int count = serviceService.updateById(serviceBean, "true");

				if(count == 1) {
					Service service = serviceService.getService(serviceBean.getName(),serviceBean.getGroup());

					if(service != null){
						String content = String.format("edited service %s for group [%s], address is %s",
								serviceBean.getName(), serviceBean.getGroup(), serviceBean.getHosts());
						workThreadPool.submit(new LogOpRun(request, OpType.UPDATE_PIGEON_SERVICE, content, projectId));

						result = Result.createSuccessResult(service);
					} else {
						result = Result.createErrorResult("server error!");
					}

				} else {
					result = Result.createErrorResult("service update failed!");
				}

			}else if("del".equals(oper)){
				serviceService.deleteByIdSplitByComma(serviceBean.getId(), "true");

				String content = String.format("deleted services, ids: %s", serviceBean.getId());
				workThreadPool.submit(new LogOpRun(request, OpType.DELETE_PIGEON_SERVICE, content, projectId));
				result = Result.createSuccessResult(content);

			}else if("add".equals(oper)){
				int count = serviceService.create(serviceBean, "true");
				Service service = serviceService.getService(serviceBean.getName(),serviceBean.getGroup());

				if(count == 1 && service != null) {
					String content = String.format("created service %s for group [%s], address is %s",
							serviceBean.getName(), serviceBean.getGroup(), serviceBean.getHosts());
					workThreadPool.submit(new LogOpRun(request, OpType.CREATE_PIGEON_SERVICE, content, projectId));

					result = Result.createSuccessResult(service);

				} else if(count < 1 && service != null) {

					if(service.getProjectid().equals(projectId)) {
						//response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "service already exists!");
						result = Result.createErrorResult("service already exists!");
					} else {
						Project project = projectService.retrieveProjectById(projectId);
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						result = Result.createErrorResult("service already exists in other project: " + project.getName());
					}

				} else if(count < 1 && service == null) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					result = Result.createErrorResult("create service error!");
				}

			}else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				result = Result.createErrorResult("oper error");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("update zk error");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			result = Result.createErrorResult("update zk error");
		}

		return result;
	}

	/**
	 * 显示所有服务配置页面（用处不大）
	 * @param modelMap
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = {"/services.all"}, method = RequestMethod.GET)
	public String allinone(ModelMap modelMap,
			HttpServletRequest request, HttpServletResponse response) {
		commonnav(modelMap, request);
		
		return "services/all";
	}

	/**
	 * 查询所有服务配置接口（用处不大）
	 * @param jqGridReqBean
	 * @return
	 */
	@RequestMapping(value = {"/services.json"}, method = RequestMethod.POST)
	@ResponseBody
	public JqGridRespBean servicesjson(ModelMap modelMap, JqGridReqBean jqGridReqBean,
			HttpServletRequest request, HttpServletResponse response) {
		
		JqGridReqFilters filters = null;
		
		if(StringUtils.isNotBlank(jqGridReqBean.getFilters())){
			
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				filters = objectMapper.readValue(jqGridReqBean.getFilters(), JqGridReqFilters.class);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		JqGridRespBean jqGridTableBean;
		
		int page = jqGridReqBean.getPage();
		int rows = jqGridReqBean.getRows();
		
		if(page > 0 && rows > 0){
			jqGridTableBean = serviceService.retrieveByJqGrid(page, rows);
		}else{
			jqGridTableBean = serviceService.retrieveByJqGrid(1, 10);
		}
		
		return jqGridTableBean;
	}

	private class LogOpRun implements Runnable {


		private final HttpServletRequest request;
		private final OpType opType;
		private final String content;
		private final Integer projectId;

		private LogOpRun(HttpServletRequest request,
						 OpType opType,
						 String content,
						 Integer projectId) {
			this.request = request;
			this.opType = opType;
			this.content = content;
			this.projectId = projectId;
		}

		@Override
		public void run() {
			User user = (User) request.getSession().getAttribute(Constants.DP_USER);
			String currentUser = user.getDpaccount();
			String reqIp = IPUtils.getUserIP(request);
			OpLog opLog = new OpLog();
			opLog.setDpaccount(currentUser);
			opLog.setProjectid(projectId);
			opLog.setReqip(reqIp);
			opLog.setOptime(new Date());
			opLog.setContent(content);
			opLog.setOptype(opType.getValue());
			opLogService.create(opLog);
		}

	}
}
