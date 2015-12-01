package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.console.Utils;
import com.dianping.pigeon.governor.bean.*;
import com.dianping.pigeon.governor.model.*;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
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
						String content = String.format("edit srv=%s&grp=%s&hsts=%s",
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

				String content = String.format("del srvs=%s&grps=%s&hsts=%s",
						serviceBean.getName(), serviceBean.getGroup(), serviceBean.getHosts());
				workThreadPool.submit(new LogOpRun(request, OpType.DELETE_PIGEON_SERVICE, content, projectId));
				result = Result.createSuccessResult(content);

			}else if("add".equals(oper)){
				int count = serviceService.create(serviceBean, "true");
				Service service = serviceService.getService(serviceBean.getName(),serviceBean.getGroup());

				if(count == 1 && service != null) {
					String content = String.format("add srv=%s&grp=%s&hsts=%s",
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

	@RequestMapping(value = {"/services/oneClickAdd"}, method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Result oneClickAdd(@RequestParam(value="ip") final String ip,
							  @RequestParam(value="port") final String port,
							  HttpServletRequest request, HttpServletResponse response) {
		Result result = null;
		String url_base = "http://" + ip + ":" + port + "/services";
		String online_url = url_base + ".publish?sign=" + Utils.getSign();
		String onlineResult = RestCallUtils.getRestCall(online_url,String.class);

		if(onlineResult != null && onlineResult.startsWith("ok")) {
			result = Result.createSuccessResult("");
		} else {
			result = Result.createErrorResult("http call error or no services found: " + url_base);
		}

		return result;
	}

	@RequestMapping(value = {"/services/oneClickOff"}, method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Result oneClickOff(@RequestParam(value="host") final String host,
							  @RequestParam(value="group") final String group,
							  @RequestParam(value="projectId") final int projectId,
							  HttpServletRequest request, HttpServletResponse response) {
		List<Service> services = serviceService.getServiceList(projectId, group);

		for(Service service : services) {
			HashSet<String> set = new HashSet<String>(Arrays.asList(service.getHosts().split(",")));
			set.remove(host);
			service.setHosts(StringUtils.join(set, ","));
			try {
				serviceService.updateById(service, "true");
			} catch (Exception e) {
				logger.error("update service error!", e);
			}
		}

		String content = String.format("Onclick Off host=%s&grp=%s",
				host, group);
		workThreadPool.submit(new LogOpRun(request, OpType.DELETE_PIGEON_SERVICE, content, projectId));

		return Result.createSuccessResult("");
	}


	@Deprecated
	@RequestMapping(value = {"/services/oneClickAdd2"}, method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Result oneClickAdd2(@RequestParam(value="ip") final String ip,
							  @RequestParam(value="port") final String port,
							  HttpServletRequest request, HttpServletResponse response) {
		Result result = null;
		String url_base = "http://" + ip + ":" + port + "/services";
		String online_url = url_base + ".online?sign=" + Utils.getSign();
		String onlineResult = RestCallUtils.getRestCall(online_url,String.class);

		if(onlineResult != null && onlineResult.startsWith("ok")) {
			String status_url = url_base + ".status";
			ServiceStatusBean serviceStatusBean = RestCallUtils.getRestCall(status_url, ServiceStatusBean.class);

			if(serviceStatusBean != null && "2.6.4".compareTo(serviceStatusBean.getVersion()) > 0) {//2.6.4以前老版本
				String json_url = url_base + ".json";
				ConsoleServiceJsonBean consoleServiceJsonBean = RestCallUtils.getRestCall(json_url, ConsoleServiceJsonBean.class);

				if(consoleServiceJsonBean != null && consoleServiceJsonBean.getServices().length > 0) {

					if ("true".equals(consoleServiceJsonBean.getPublished())) {
						String rPort = consoleServiceJsonBean.getPort().split("/")[0];
						String host = ip + ":" + rPort;

						String projectName = consoleServiceJsonBean.getApp();
						String group = consoleServiceJsonBean.getGroup();
						Project project = projectService.findProject(projectName);
						int projectId = project.getId();
						HashSet<Service> existServices = new HashSet<Service>(serviceService.getServiceList(projectId, group));
						ConsoleService[] consoleServices = consoleServiceJsonBean.getServices();

						for(ConsoleService consoleService : consoleServices) {

							Service tService = null;
							for(Service eService : existServices) {
								if(consoleService.getName().equals(eService.getName())) {
									HashSet<String> set = new HashSet<String>();

									for(String eHost : eService.getHosts().split(",")){
										if(StringUtils.isNotBlank(eHost)){
											set.add(eHost);
										}
									}

									set.add(host);
									eService.setHosts(StringUtils.join(set, ","));
									try {
										serviceService.updateById(eService, "true");
									} catch (Exception e) {
										logger.error("update service error!", e);
									}
									tService = eService;
									break;
								}
							}

							if(tService != null) {
								existServices.remove(tService);
							} else {
								tService = new Service();
								tService.setName(consoleService.getName());
								tService.setGroup(group);
								tService.setHosts(host);
								tService.setProjectid(projectId);
								try {
									serviceService.create(tService, "true");
								} catch (Exception e) {
									logger.error("create service error!", e);
									result = Result.createErrorResult("create service error!");
								}
							}

						}

						result = Result.createSuccessResult("");
					} else {
						result = Result.createErrorResult("error, auto publish is set to false.");
					}

				} else {
					result = Result.createErrorResult("http call error or no services found: " + url_base);
				}
			} else if(serviceStatusBean != null) {//2.6.4及以上版本调用services.online接口会自动注册
				result = Result.createSuccessResult("");
			} else {
				result = Result.createErrorResult("http call error or no services found: " + url_base);
			}

		} else {
			result = Result.createErrorResult("http call error or no services found: " + url_base);
		}

		return result;
	}

	@Deprecated
	@RequestMapping(value = {"/services/oneClickAdd3"}, method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Result oneClickAdd3(@RequestParam(value="ip") final String ip,
							  @RequestParam(value="port") final String port,
							  HttpServletRequest request, HttpServletResponse response) {
		String url = "http://" + ip + ":" + port + "/services.json";
		ConsoleServiceJsonBean consoleServiceJsonBean = RestCallUtils.getRestCall(url,ConsoleServiceJsonBean.class);

		if(consoleServiceJsonBean != null && consoleServiceJsonBean.getServices().length > 0) {
			String projectName = consoleServiceJsonBean.getApp();
			Project project = projectService.findProject(projectName);
			List<Service> services = serviceService.getServiceList(project.getId());

			return Result.createSuccessResult("");
		} else {

			return Result.createErrorResult("http call error or no services found: " + url);
		}

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
