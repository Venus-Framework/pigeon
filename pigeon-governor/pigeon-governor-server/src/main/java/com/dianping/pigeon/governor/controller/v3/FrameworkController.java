package com.dianping.pigeon.governor.controller.v3;

import com.dianping.pigeon.governor.bean.serviceDesc.ServiceDescBean;
import com.dianping.pigeon.governor.bean.serviceTree.TreeNode;
import com.dianping.pigeon.governor.controller.BaseController;
import com.dianping.pigeon.governor.message.depenedencies.service.EventReceiverService;
import com.dianping.pigeon.governor.model.EventReceiverModel;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.ProjectOrg;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.*;
import com.dianping.pigeon.governor.task.organization.OrgUpdateTask;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.dianping.pigeon.governor.util.UserRole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by shihuashen on 16/9/2.
 */
@Controller
public class FrameworkController extends BaseController{
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ServiceTreeService serviceTreeService;
    @Autowired
    private DBSearchService dbSearchService;
    @Autowired
    private ServiceDescService serviceDescService;
    @Autowired
    private ProjectOwnerService projectOwnerService;
    private InnerCache cache;
    @Autowired
    private OrgUpdateTask orgUpdateTask;
    @PostConstruct
    public void init(){
        this.cache = new InnerCache(2,2);
        this.cache.schedule();
    }
    private Logger logger = LogManager.getLogger(FrameworkController.class.getName());
    class InnerCache implements Runnable{
        private volatile TreeNode projectTree;
        private volatile JSONArray projectTypeAhead;
        private volatile JSONArray serviceTypeAhead;
        private ScheduledExecutorService service;
        private int delayTime;
        public InnerCache(int poolSize,int delayTime){
            this.projectTree = serviceTreeService.getFullTree();
            this.projectTypeAhead = dbSearchService.getProjectTypeAheadInfo();
            this.serviceTypeAhead = dbSearchService.getServiceTypeAheadInfo();
            this.service = Executors.newScheduledThreadPool(poolSize);
            this.delayTime = delayTime;
        }
        public void schedule(){
            service.scheduleAtFixedRate(
                    this,0,
                    delayTime, TimeUnit.MINUTES);
        }
        public TreeNode getProjectTree(){
            return this.projectTree;
        }
        public JSONArray getProjectTypeAhead(){
            return this.projectTypeAhead;
        }
        public JSONArray getServiceTypeAhead(){
            return this.serviceTypeAhead;
        }
        @Override
        public void run() {
            GsonUtils.Print("start refresh cache");
            TreeNode treeNode = serviceTreeService.getFullTree();
            JSONArray projectTypeAhead = dbSearchService.getProjectTypeAheadInfo();
            JSONArray serviceTypeAhead = dbSearchService.getServiceTypeAheadInfo();
            this.projectTree = treeNode;
            this.projectTypeAhead = projectTypeAhead;
            this.serviceTypeAhead = serviceTypeAhead;
        }
    }
    @RequestMapping(value={"/"},method = RequestMethod.GET)
    public String entrance(HttpServletRequest request,
                           HttpServletResponse response,
                           ModelMap modelMap){
        commonnav(modelMap, request);
        return "/v3/common/framework";
    }


    @RequestMapping(value = {"/framework/doc"},method = RequestMethod.GET)
    public String docPage(HttpServletRequest request,
                          HttpServletResponse response,
                          ModelMap modelMap){
        commonnav(modelMap, request);
        return "/v3/serviceDoc/esSearch";
    }
    @RequestMapping(value={"/framework/doc/{serviceId}"},method = RequestMethod.GET)
    public String docDetail(HttpServletRequest request,
                            HttpServletResponse response,
                            @PathVariable int serviceId,
                            ModelMap modelMap){
        commonnav(modelMap, request);
        User user = getUserInfo(request);
        ServiceDescBean bean = serviceDescService.getServiceDescBeanById(serviceId);
        if(bean!=null){
            String dpAccount = user!=null?user.getDpaccount():"null";
            modelMap.put("serviceDescBean", bean);
            Map<String,Object> metaInfo = serviceDescService.getServiceMetaInfoById(serviceId);
            modelMap.addAllAttributes(metaInfo);
            if(UserRole.USER_SCM.getValue().equals(user.getRoleid()) ||
                    projectOwnerService.isProjectOwner(dpAccount,metaInfo.get("projectName").toString()))
                modelMap.put("empowered",true);
            else
                modelMap.put("empowered",false);
            return "/v3/serviceDoc/serviceDoc";
        }else{
            return "/doc/emptyDoc";
        }
    }
    @RequestMapping(value = {"/framework/oplog"},method = RequestMethod.GET)
    public String mainPage(HttpServletRequest request,
                           HttpServletResponse response,
                           ModelMap modelMap){
        commonnav(modelMap,request);
        List<Project> projects = projectService.retrieveAllIdNamesByCache();
        modelMap.addAttribute("projects", GsonUtils.toJson(projects));
        return "/v3/log/oplog/main";
    }


    @RequestMapping(value = {"/framework/event"},method = RequestMethod.GET)
    public String mainEntrance(HttpServletRequest request,
                               HttpServletResponse response,
                               ModelMap modelMap){
        commonnav(modelMap,request);
        List<Project> projects = projectService.retrieveAllIdNamesByCache();
        modelMap.addAttribute("projects",GsonUtils.toJson(projects));
        return "/v3/log/eventlog/main";
    }

    @RequestMapping(value = {"/framework/feedback"},method = RequestMethod.GET)
    public String feedbackMainPage(HttpServletRequest request,HttpServletResponse response,ModelMap modelMap){
        commonnav(modelMap,request);
        return "/v3/feedback/main";
    }
    @RequestMapping(value={"/framework/help"},method = RequestMethod.GET)
    public String helpMainPage(HttpServletRequest request,
                               HttpServletResponse response,
                               ModelMap modelMap){
        return "/v3/help/main";
    }

    @RequestMapping(value={"/tree/sidebar"},method = RequestMethod.GET)
    public String sideBar(HttpServletRequest request,
                          HttpServletResponse response,
                          ModelMap modelMap){
        commonnav(modelMap,request);
        User user = getUserInfo(request);
        String dpAccount = user.getDpaccount();
        TreeNode root = cache.getProjectTree();
        modelMap.put("data",GsonUtils.toJson(root));
        Set<String> projects = serviceTreeService.getMyProject(dpAccount);
        modelMap.put("projects",projects);
        return "/v3/common/sideBar";
    }

    @RequestMapping(value={"/govern/{projectName}"},method = RequestMethod.GET)
    public String projectGovern(HttpServletRequest request,
                                HttpServletResponse response,
                                @PathVariable String projectName,
                                ModelMap modelMap){
        ProjectOrg projectOrg = serviceTreeService.getProjectOrg(projectName);
        modelMap.put("projectOrg",projectOrg);
        return "/v3/project/projectGovern";
    }
    @RequestMapping(value = {"/project/list.json"}, method= RequestMethod.GET)
    public void typeAheadProjectInfo(HttpServletRequest request,
                                     HttpServletResponse response,
                                     ModelMap modelMap){
        response.setContentType("application/json,charset=UTF-8");
        PrintWriter pw = null;
        try {
            pw = response.getWriter();
            pw.write(cache.getProjectTypeAhead().toString());

        } catch (IOException e) {
            logger.error(e);
        }
    }

    @RequestMapping(value={"/service/list.json"},method=RequestMethod.GET)
    public void typeAheadServiceInfo(HttpServletRequest request,
                                     HttpServletResponse response,
                                     ModelMap modelMap){
        response.setContentType("application/json.charset=UTF-8");
        PrintWriter pw = null;
        try {
            pw = response.getWriter();
            pw.write(cache.getServiceTypeAhead().toString());
        } catch (IOException e) {
            logger.error(e);
        }
    }

    @RequestMapping(value={"/framework/startOrgFetch"},method = RequestMethod.GET)
    public void startOrgFetch(HttpServletRequest request,
                              HttpServletResponse response,
                              ModelMap modelMap){
        this.orgUpdateTask.start();
    }
}
