package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.Result;
import com.dianping.pigeon.governor.bean.ServiceHostsBean;
import com.dianping.pigeon.governor.bean.ServiceUpdateBean;
import com.dianping.pigeon.governor.bean.ServiceWithGroup;
import com.dianping.pigeon.governor.model.*;
import com.dianping.pigeon.governor.service.*;
import com.dianping.pigeon.governor.util.*;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by chenchongze on 16/7/7.
 */
@Controller
public class ServiceNodeController extends BaseController {

    private Logger logger = LogManager.getLogger();
    private ExecutorService workThreadPool = ThreadPoolFactory.getWorkThreadPool();

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectOwnerService projectOwnerService;
    @Autowired
    private ServiceNodeService serviceNodeService;
    @Autowired
    private OpLogService opLogService;
    @Autowired
    @Qualifier("registrySerivce")
    private RegistryService registryService;

    private Gson gson = new Gson();


    @RequestMapping(value = {"/services/{projectName:.+}"}, method = RequestMethod.GET)
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

        return "/serviceNodes/list4project";
    }

    @RequestMapping(value = {"/services/add/{projectName:.+}"}, method = RequestMethod.POST)
    @ResponseBody
    public Result servicesAdd(ServiceUpdateBean serviceUpdateBean,
                                      @PathVariable String projectName,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        Result result = null;

        try {
            Set<String> toAddHosts = new HashSet<String>(serviceUpdateBean.getToAddHosts());
            String serviceName = serviceUpdateBean.getServiceName();
            String group = serviceUpdateBean.getGroup();

            // 添加服务节点
            Set<String> addHosts2db = updateAddHosts(toAddHosts, serviceName, group, projectName);

            // 从zk拉取服务列表尝试更新
            String hostsZk = registryService.getServiceHosts(serviceName, group);
            Set<String> toUpdateHosts = Sets.newHashSet(hostsZk.split(","));
            toUpdateHosts.addAll(addHosts2db);
            registryService.registryUpdateService(serviceName, group, toUpdateHosts, addHosts2db);

            //打数据库日志
            String content = String.format("add srv=%s&grp=%s&hsts=%s",
                    serviceName, group, StringUtils.join(toUpdateHosts, ","));
            workThreadPool.submit(new LogOpRun(request, OpType.CREATE_PIGEON_SERVICE, content, null));

            result = Result.createSuccessResult(toUpdateHosts);
        } catch (RegistryException e) {
            logger.error(e);
            //response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Result.createErrorResult("update zk error");
            // todo zk更新失败要不要回滚？
        } catch (Throwable t) {
            logger.error(t);
            //response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Result.createErrorResult("add service node error! msg: " + t);
        }

        return result;
    }

    @RequestMapping(value = {"/services/edit/{projectName:.+}"}, method = RequestMethod.POST)
    @ResponseBody
    public Result servicesEdit(ServiceUpdateBean serviceUpdateBean,
                                      @PathVariable String projectName,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        Result result = null;

        try {
            Set<String> toAddHosts = new HashSet<String>(serviceUpdateBean.getToAddHosts());
            Set<String> toDelHosts = new HashSet<String>(serviceUpdateBean.getToDelHosts());
            String serviceName = serviceUpdateBean.getServiceName();
            String group = serviceUpdateBean.getGroup();

            // 添加服务节点
            Set<String> addHosts2db = updateAddHosts(toAddHosts, serviceName, group, projectName);

            // 删除服务节点
            Set<String> delHosts2db = updateDelHosts(toDelHosts, serviceName, group, projectName);


            // 从zk拉取服务列表尝试更新
            String hostsZk = registryService.getServiceHosts(serviceName, group);
            Set<String> toUpdateHosts = Sets.newHashSet(hostsZk.split(","));
            toUpdateHosts.addAll(addHosts2db);
            toUpdateHosts.removeAll(delHosts2db);
            registryService.registryUpdateService(serviceName, group, toUpdateHosts, addHosts2db);

            //打数据库日志
            String content = String.format("edit srv=%s&grp=%s&hsts=%s",
                    serviceName, group, StringUtils.join(toUpdateHosts, ","));
            workThreadPool.submit(new LogOpRun(request, OpType.UPDATE_PIGEON_SERVICE, content, null));

            result = Result.createSuccessResult(toUpdateHosts);
        } catch (RegistryException e) {
            logger.error(e);
            //response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Result.createErrorResult("update zk error");
            // todo zk更新失败要不要回滚？
        } catch (Throwable t) {
            logger.error(t);
            //response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Result.createErrorResult("edit service node error! msg: " + t);
        }

        return result;
    }

    private Set<String> updateDelHosts(Set<String> toDelHosts, String serviceName, String group, String projectName) {
        Set<String> delHosts2db = Sets.newHashSet();

        if (toDelHosts != null) {
            String[] validHosts = IPUtils.getValidHosts(toDelHosts.toArray(new String[toDelHosts.size()]));

            for (String toDelHost : validHosts) {
                int index = toDelHost.lastIndexOf(":");
                String ip = toDelHost.substring(0, index);
                String port = toDelHost.substring(index + 1);
                ServiceNode serviceNode = serviceNodeService.getServiceNode(serviceName, group, ip, port);

                boolean updateDbSuccess = true;

                if (serviceNode != null) {

                    int count = serviceNodeService.deleteServiceNodeById(serviceNode);

                    if (count < 0) {
                        updateDbSuccess = false;
                    }
                }

                if (updateDbSuccess) {
                    delHosts2db.add(toDelHost);
                }
            }
        }

        return delHosts2db;
    }

    private Set<String> updateAddHosts(Set<String> toAddHosts, String serviceName, String group, String projectName) {
        Set<String> addHosts2db = Sets.newHashSet();

        if (toAddHosts != null) {
            String[] validHosts = IPUtils.getValidHosts(toAddHosts.toArray(new String[toAddHosts.size()]));

            for (String toAddHost : validHosts) {
                int index = toAddHost.lastIndexOf(":");
                String ip = toAddHost.substring(0, index);
                String port = toAddHost.substring(index + 1);
                ServiceNode serviceNode = serviceNodeService.getServiceNode(serviceName, group, ip, port);

                boolean updateDbSuccess = true;

                if (serviceNode == null) {
                    serviceNode = new ServiceNode();
                    serviceNode.setServiceName(serviceName);
                    serviceNode.setGroup(group);
                    serviceNode.setIp(ip);
                    serviceNode.setPort(port);
                    serviceNode.setProjectName(projectName);

                    int count = serviceNodeService.createServiceNode(serviceNode);

                    if (count <= 0) {
                        updateDbSuccess = false;
                    }
                }

                if (updateDbSuccess) {
                    addHosts2db.add(toAddHost);
                }

            }
        }

        return addHosts2db;
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

    public static void main(String[] args) {
        new HashSet<String>(null);
    }
}
