package com.dianping.pigeon.governor.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.pigeon.governor.model.Host;
import com.dianping.pigeon.governor.model.OpLog;
import com.dianping.pigeon.governor.service.*;
import com.dianping.pigeon.governor.util.Constants;

import com.dianping.pigeon.governor.util.IPUtils;
import com.dianping.pigeon.governor.util.OpType;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.pigeon.governor.bean.Result;
import com.dianping.pigeon.governor.lion.ConfigHolder;
import com.dianping.pigeon.governor.lion.LionKeys;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.mysql.jdbc.log.Log;

/**
 * 
 * @author chenchongze
 *
 */
@Controller
@RequestMapping("/api")
public class ServiceApiController extends BaseController {
	
	private Logger logger = LogManager.getLogger();

	@Autowired
	private ServiceService serviceService;
	@Autowired
	private ProjectService projectService;
	@Autowired
    @Qualifier("registrySerivce")
	private RegistryService registryService;
    @Autowired
    private HostService hostService;

	public static final String SUCCESS_CODE = "0|";		//正确返回码
	public static final String ERROR_CODE = "1|";		//错误返回码
	
	/**
	 * 获取服务列表
	 * @author chenchongze
	 * @param project
	 * @return
	 */
	@RequestMapping(value = "/service2/list", method = RequestMethod.GET)
    @ResponseBody
    public Result list(@RequestParam(value="project") String project) {
        if(project==null) {
            return Result.createErrorResult("Project is null");
        }
        
        Project prj = projectService.findProject(project);
        if(prj == null) {
            return Result.createErrorResult(String.format("Project %s does not exist", project));
        }
        
        List<Service> serviceList = serviceService.getServiceList(prj.getId());
        List<String> srvNameList = new ArrayList<String>();
        for(Service service : serviceList) {
            srvNameList.add(service.getName());
        }
        
        return Result.createSuccessResult(srvNameList);
    }
    
	/**
	 * 获取服务地址
	 * @author chenchongze
	 * @param service
	 * @param group
	 * @return
	 */
    @RequestMapping(value = "/service2/get", method = RequestMethod.GET)
    @ResponseBody
    public Result get(@RequestParam(value="service") String service,
                      @RequestParam(value="group", required=false, defaultValue="") String group) {
        
        Service srv = serviceService.getService(service, group);
        if(srv == null) {
            return Result.createErrorResult(String.format("Service %s for group [%s] does not exist", service, group));
        } else {
            return Result.createSuccessResult(srv.getHosts());
        }
    }
    
    @RequestMapping(value = "/service/publish", method = RequestMethod.GET)
    public void publish(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam(value="id") int id,
    					@RequestParam(value="project", required=false, defaultValue="") String project,
    					@RequestParam(value="app", required=false, defaultValue="") String app,
    					@RequestParam(value="service") String service,
    					@RequestParam(value="group", required=false, defaultValue="") String group,
    					@RequestParam(value="ip") String ip,
    					@RequestParam(value="port") String port,
    					@RequestParam(value="updatezk", required=false, defaultValue="") String updatezk,
    					@RequestParam(value="op", required=false, defaultValue="") String op) throws IOException {
        
    	response.setContentType("text/plain;charset=utf-8");
        PrintWriter writer = response.getWriter();
    	
    	try {
            verifyIdentity(id);
        } catch (Exception e) {
        	writer.write(ERROR_CODE + e.getMessage());
        	return;
        }
    	
    	String appname = StringUtils.isBlank(project) ? app : project;
        if(StringUtils.isBlank(appname)) {
            writer.write(ERROR_CODE + String.format("Service %s for group [%s]'s appname is blank!", service, group));
            return;
        }

    	String hosts = null;
		try {
			hosts = serviceService.publishService(appname, service, group, ip, port, 
										ConfigHolder.get(LionKeys.IS_ZK_DOUBLE_WRITE, "false"));


            /*Host host = hostService.retrieveByIpPort(ip,port);

            if(host != null && host.getRegistry() == Constants.HOST_REGISTRY_LION) {
                host.setRegistry(Constants.HOST_REGISTRY_PIGEON);
                hostService.update(host);
            }

            if(host == null) {
                host = new Host();
                host.setIpport(ip + ":" + port);
                host.setAppname(appname);
                host.setRegistry(Constants.HOST_REGISTRY_PIGEON);
                hostService.create(host);
            }*/

            if(StringUtils.isNotBlank(op))
				logger.info("publish op is: " + op);
		} catch (Exception e) {
			writer.write(ERROR_CODE + String.format("Service %s for group [%s] update to ZK failed", service, group));
			return;
		}
    	
        if(hosts == null) {
        	writer.write(ERROR_CODE + String.format("Service %s for group [%s] update to DB failed", service, group));
        } else {
        	writer.write(SUCCESS_CODE + hosts);
            String reqIp = IPUtils.getUserIP(request);
            String content = String.format("%s published service %s for group [%s], address is %s", reqIp, service, group, ip+":"+port);
            logger.info(content);
        }
        
    }
    
    @RequestMapping(value = "/service/unpublish", method = RequestMethod.GET)
    public void unpublish(HttpServletRequest request,
                          HttpServletResponse response,
                          @RequestParam(value="id") int id,
                          @RequestParam(value="service") String service,
                          @RequestParam(value="group", required=false, defaultValue="") String group,
                          @RequestParam(value="ip") String ip,
                          @RequestParam(value="port") String port,
                          @RequestParam(value="updatezk", required=false, defaultValue="") String updatezk,
                          @RequestParam(value="op", required=false, defaultValue="") String op) throws IOException {
    	
    	response.setContentType("text/plain;charset=utf-8");
    	PrintWriter writer = response.getWriter();
    	
    	try {
            verifyIdentity(id);
        } catch (Exception e) {
        	writer.write(ERROR_CODE + e.getMessage());
        	return;
        }

    	String hosts = null;
		try {
			hosts = serviceService.unpublishService(service, group, ip, port, 
									ConfigHolder.get(LionKeys.IS_ZK_DOUBLE_WRITE, "false"));
			
			if(StringUtils.isNotBlank(op))
				logger.info("unpublish op is: " + op);
		} catch (Exception e) {
			writer.write(ERROR_CODE + String.format("Service %s for group [%s] update to ZK failed", service, group));
		}
    	
        if(hosts == null) {
        	writer.write(ERROR_CODE + String.format("Service %s for group [%s] update to DB failed", service, group));
        } else {
        	writer.write(SUCCESS_CODE + hosts);
            String reqIp = IPUtils.getUserIP(request);
            String content = String.format("%s unpublished service %s for group [%s], address is %s", reqIp, service, group, ip+":"+port);
            logger.info(content);
        }
    }

    /**
     * 设置服务地址
     * @author chenchongze
     * @param request
     * @param id
     * @param service
     * @param group
     * @param address
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/service2/set", method = RequestMethod.PUT)
    @ResponseBody
    public Result set(HttpServletRequest request,
                      @RequestParam(value="id") int id,
                      @RequestParam(value="service") String service,
                      @RequestParam(value="group", required=false, defaultValue="") String group,
                      @RequestParam(value="address") String address) {
        try {
            verifyIdentity(id);
        } catch (Exception e) {
            return Result.createErrorResult(e.getMessage());
        }

        Service oriService = serviceService.getService(service, group);

        if(oriService == null) {

            return Result.createErrorResult(String.format("Service %s for group [%s] does not exist", service, group));

        } else {
            Service newService = new Service(oriService);
            newService.setHosts(address);

            try {
                serviceService.updateById(newService);
                registryService.registryUpdateService(oriService, newService);

                String message = String.format("Updated service %s for group [%s] to address %s", service, group, address);
                return Result.createSuccessResult(message);
            } catch (Exception e) {
                return Result.createErrorResult(String.format("Failed to update service %s: %s",service, e.getMessage()));
            }

        }

    }
}
