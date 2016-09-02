package com.dianping.pigeon.governor.controller.v3;

import com.dianping.pigeon.governor.bean.serviceTree.TreeNode;
import com.dianping.pigeon.governor.controller.BaseController;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.ProjectOrg;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.DBSearchService;
import com.dianping.pigeon.governor.service.FeedbackService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.ServiceTreeService;
import com.dianping.pigeon.governor.util.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

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
    @RequestMapping(value = {"/framework/oplog"},method = RequestMethod.GET)
    public String mainPage(HttpServletRequest request,
                           HttpServletResponse response,
                           ModelMap modelMap){
        commonnav(modelMap,request);
        List<Project> projects = projectService.retrieveAllIdNamesByCache();
        modelMap.addAttribute("projects", GsonUtils.toJson(projects));
        return "/v3/log/opLog/main";
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

    @RequestMapping(value={"/tree/sidebar"},method = RequestMethod.GET)
    public String sideBar(HttpServletRequest request,
                          HttpServletResponse response,
                          ModelMap modelMap){
        commonnav(modelMap,request);
        User user = getUserInfo(request);
        String dpAccount = user.getDpaccount();
        TreeNode root = serviceTreeService.getFullTree();
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
            pw.write(dbSearchService.getProjectTypeAheadInfo().toString());

        } catch (IOException e) {
            e.printStackTrace();
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
            pw.write(dbSearchService.getServiceTypeAheadInfo().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
