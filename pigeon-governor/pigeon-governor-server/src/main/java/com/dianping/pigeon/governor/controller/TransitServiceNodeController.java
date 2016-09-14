package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.ServiceHostsBean;
import com.dianping.pigeon.governor.bean.ServiceWithGroup;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.ServiceNode;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.*;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.IPUtils;
import com.dianping.pigeon.governor.util.ThreadPoolFactory;
import com.dianping.pigeon.governor.util.UserRole;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created by shihuashen on 16/8/16.
 */
@Controller
public class TransitServiceNodeController extends BaseController {

    private ExecutorService workThreadPool = ThreadPoolFactory.getWorkThreadPool();
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectOwnerService projectOwnerService;
    @Autowired
    private ServiceNodeService serviceNodeService;
    private Gson gson = new Gson();
    @RequestMapping(value = {"/transit/services/{projectName:.+}"}, method = RequestMethod.GET)
    public String projectInfo(ModelMap modelMap,
                              @PathVariable final String projectName,
                              HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(Constants.DP_USER);
        String currentUser = user.getDpaccount();
        modelMap.addAttribute("userName", currentUser);
        Project project = projectService.findProject(projectName);

        if(project == null){
            project = projectService.createProjectFromCmdbOrNot(projectName);

            if(project == null){
                modelMap.addAttribute("errorMsg", "cmdb找不到项目或服务从未自动发布过：" + projectName);
                return "/error/500";
            }

            final String emails = project.getEmail();
            workThreadPool.execute(new Runnable() {
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

        List<ServiceNode> serviceNodeList = serviceNodeService.retrieveAllByProjectName(projectName);
        Map<ServiceWithGroup, ServiceHostsBean> serviceMap = Maps.newHashMap();

        for (ServiceNode serviceNode : serviceNodeList) {
            String serviceName = serviceNode.getServiceName();
            String group = serviceNode.getGroup();
            String host = IPUtils.getHost(serviceNode.getIp(), serviceNode.getPort());

            ServiceWithGroup serviceWithGroup
                    = new ServiceWithGroup(serviceNode.getServiceName(), serviceNode.getGroup());
            ServiceHostsBean service = serviceMap.get(serviceWithGroup);

            if (service != null) {
                service.setHosts(service.getHosts() + "," + host);
            } else {
                ServiceHostsBean newService = new ServiceHostsBean();
                newService.setHosts(host);
                newService.setName(serviceName);
                newService.setGroup(group);

                serviceMap.put(serviceWithGroup, newService);
            }
        }

        modelMap.addAttribute("services", serviceMap.values());

        return "/transit/serviceNodes/list4project";
    }
}
