package com.dianping.pigeon.governor.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.pigeon.governor.bean.Result;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.ServiceService;
import com.dianping.pigeon.registry.exception.RegistryException;

/**
 * 
 * @author chenchongze
 *
 */
@Controller
@RequestMapping("/api")
public class ServiceApiController extends BaseController {

	@Autowired
	private ServiceService serviceService;
	@Autowired
	private ProjectService projectService;
	
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
    @RequestMapping(value = "/service2/set", method = RequestMethod.GET)
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
                serviceService.registryUpdateService(oriService, newService);
                
                String message = String.format("Updated service %s for group [%s] to address %s", service, group, address);
                return Result.createSuccessResult(message);
            } catch (Exception e) {
                return Result.createErrorResult(String.format("Failed to update service %s: %s",service, e.getMessage()));
            }
            
        }
        
    }
    
    @RequestMapping(value = "/service/publish", method = RequestMethod.GET)
    public void publish(HttpServletResponse response, @RequestParam(value="id") int id,
    						@RequestParam(value="project", required=false, defaultValue="") String project,
    						@RequestParam(value="app", required=false, defaultValue="") String app,
    						@RequestParam(value="service") String service,
    						@RequestParam(value="group", required=false, defaultValue="") String group,
    						@RequestParam(value="ip") String ip,
    						@RequestParam(value="port") String port,
    						@RequestParam(value="updatezk", required=false, defaultValue="") String updatezk) throws IOException {
        
    	response.setContentType("text/plain;charset=utf-8");
        PrintWriter writer = response.getWriter();
    	
    	try {
            verifyIdentity(id);
        } catch (Exception e) {
        	writer.write(ERROR_CODE + e.getMessage());
        	return;
        }
    	
    	String appname = StringUtils.isBlank(project) ? app : project;

    	String hosts = null;
		try {
			hosts = serviceService.publishService(appname, service, group, ip, port, updatezk);
		} catch (RegistryException e) {
			writer.write(ERROR_CODE + String.format("Service %s for group [%s] update to ZK failed", service, group));
		}
    	
        if(hosts == null) {
        	writer.write(ERROR_CODE + String.format("Service %s for group [%s] update to DB failed", service, group));
        } else {
        	writer.write(SUCCESS_CODE + hosts);
        }
        
    }
    
    @RequestMapping(value = "/service/unpublish", method = RequestMethod.GET)
    public void unpublish(HttpServletResponse response, @RequestParam(value="id") int id,
    						@RequestParam(value="service") String service,
    						@RequestParam(value="group", required=false, defaultValue="") String group,
    						@RequestParam(value="ip") String ip,
    						@RequestParam(value="port") String port,
    						@RequestParam(value="updatezk", required=false, defaultValue="") String updatezk) throws IOException {
    	
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
			hosts = serviceService.unpublishService(service, group, ip, port, updatezk);
		} catch (RegistryException e) {
			writer.write(ERROR_CODE + String.format("Service %s for group [%s] update to ZK failed", service, group));
		}
    	
        if(hosts == null) {
        	writer.write(ERROR_CODE + String.format("Service %s for group [%s] update to DB failed", service, group));
        } else {
        	writer.write(SUCCESS_CODE + hosts);
        }
    }

}
