package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.providerFlowLimit.AppLimit;
import com.dianping.pigeon.governor.bean.providerFlowLimit.MethodAppLimit;
import com.dianping.pigeon.governor.exception.LionNullProjectException;
import com.dianping.pigeon.governor.exception.LionValuePraseErrorException;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.ServiceLimitService;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.dianping.pigeon.governor.util.UserRole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by shihuashen on 16/9/12.
 */
@Controller
public class ServiceLimitController extends BaseController{
    private Logger logger = LogManager.getLogger(ServiceLimitController.class.getName());
    @Autowired
    private ServiceLimitService serviceLimitService;
    @Autowired
    private ProjectOwnerService projectOwnerService;
    @Autowired
    private ProjectService projectService;
    @RequestMapping(value={"/config/limit/"},method = RequestMethod.POST)
    public String main(HttpServletRequest request,
                               HttpServletResponse response,
                               ModelMap modelMap){
        User user = getUserInfo(request);
        String projectName = request.getParameter("projectName");
        boolean empowered = false;
        if(UserRole.USER_SCM.getValue().equals(user.getRoleid()) ||
                projectOwnerService.isProjectOwner(user.getDpaccount(),projectName))
            empowered = true;
        modelMap.put("empowered",empowered);
        List<Project> projects = projectService.retrieveAllIdNamesByCache();
        modelMap.addAttribute("projects", GsonUtils.toJson(projects));
        try {
            boolean appLimitState = serviceLimitService.getAppLimitState(projectName);
            boolean methodLimitState = serviceLimitService.getMethodLimitState(projectName);
            modelMap.put("appLimitState",appLimitState);
            modelMap.put("methodLimitState",methodLimitState);
        } catch (LionNullProjectException e) {
            logger.info(e);
            modelMap.put("error","Project: "+projectName+" wasn't created in Lion.");
            return "/config/project/LionLack";
        }
        return "/v3/limitation/main";
    }
    @RequestMapping(value={"/applimit/enable/"},method = RequestMethod.POST)
    public void enableAppLimit(HttpServletRequest request,
                               HttpServletResponse response,
                               ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        boolean opState = this.serviceLimitService.setAppLimitState(projectName,true);
        response.setCharacterEncoding("UTF-8");
        try {
            PrintWriter out = response.getWriter();
            out.write(String.valueOf(opState));
        } catch (IOException e) {
            logger.warn(e);
        }
    }
    @RequestMapping(value={"/applimit/disable/"},method = RequestMethod.POST)
    public void disableAppLimit(HttpServletRequest request,
                                HttpServletResponse response,
                                ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        boolean opState = this.serviceLimitService.setAppLimitState(projectName,false);
        response.setCharacterEncoding("UTF-8");
        try {
            PrintWriter out = response.getWriter();
            out.write(String.valueOf(opState));
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    @RequestMapping(value = {"/methodapplimit/enable/"},method = RequestMethod.POST)
    public void enableMethodAppLimit(HttpServletRequest request,
                                     HttpServletResponse response,
                                     ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        boolean opState = this.serviceLimitService.setMethodAppLimit(projectName,true);
        response.setCharacterEncoding("UTF-8");
        try {
            PrintWriter out = response.getWriter();
            out.write(String.valueOf(opState));
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    @RequestMapping(value = {"/methodapplimit/disable/"},method = RequestMethod.POST)
    public void disableMethodAppLimit(HttpServletRequest request,
                                      HttpServletResponse response,
                                      ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        boolean opState = this.serviceLimitService.setMethodAppLimit(projectName,false);
        response.setCharacterEncoding("UTF-8");
        try {
            PrintWriter out = response.getWriter();
            out.write(String.valueOf(opState));
        } catch (IOException e) {
            logger.warn(e);
        }
    }


    @RequestMapping(value = {"/applimit/table"},method = RequestMethod.POST)
    public String getTable(HttpServletRequest request,
                           HttpServletResponse response,
                           ModelMap modelMap) {
        String projectName = request.getParameter("projectName");
        AppLimit appLimit = null;
        try{
            appLimit = serviceLimitService.getAppLimit(projectName);
        } catch (LionNullProjectException e) {
            logger.info(e);
        } catch (LionValuePraseErrorException e) {
            logger.error(e);
            modelMap.put("error","Project: "+projectName+" configs parse error!!");
            return "/v3/limitation/parse-error";
        }
        modelMap.put("appConfigs",appLimit);
        return "/v3/limitation/app-limit-table";
    }

    @RequestMapping(value = {"/methodlimit/table"},method = RequestMethod.POST)
    public String getMethodsTable(HttpServletRequest request,
                                  HttpServletResponse response,
                                  ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        MethodAppLimit methodAppLimit = null;
        try{
            methodAppLimit = serviceLimitService.getMethodAppLimit(projectName);
        } catch (LionValuePraseErrorException e) {
            logger.info(e);
        } catch (LionNullProjectException e) {
            logger.error(e);
            modelMap.put("error","Project: "+projectName+" configs parse error!!");
            return "/v3/limitation/parse-error";
        }
        modelMap.put("configs",methodAppLimit.getConfigs());
        return "/v3/limitation/method-limit-table";
    }
    @RequestMapping(value={"/applimit/add"},method = RequestMethod.POST)
    public void  addAppLimit(HttpServletRequest request,
                              HttpServletResponse response,
                              ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        String appName = request.getParameter("appName");
        String qps = request.getParameter("qps");
        boolean status = false;
        try {
           status =  serviceLimitService.addAppLimit(projectName,appName,Long.valueOf(qps));
        } catch (LionValuePraseErrorException e) {
            logger.error(e);
        }
        response.setCharacterEncoding("UTF-8");
        try {
            PrintWriter out = response.getWriter();
            out.write(String.valueOf(status));
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    @RequestMapping(value={"/applimit/update"},method = RequestMethod.POST)
    public void updateAppLimit(HttpServletRequest request,
                               HttpServletResponse response,
                               ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        String appName = request.getParameter("appName");
        String qps = request.getParameter("qps");
        boolean status = false;
        try{
            status = serviceLimitService.updateAppLimit(projectName,appName,Long.valueOf(qps));
        } catch (LionValuePraseErrorException e) {
            logger.error(e);
        }
        response.setCharacterEncoding("UTF-8");
        try {
            PrintWriter out = response.getWriter();
            out.write(String.valueOf(status));
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    @RequestMapping(value={"/applimit/delete"},method = RequestMethod.POST)
    public void deleteAppLimit(HttpServletRequest request,
                               HttpServletResponse response,
                               ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        String appName = request.getParameter("appName");
        String qps = request.getParameter("qps");
        boolean status = false;
        try{
            status = serviceLimitService.removeAppLimit(projectName,appName,Long.valueOf(qps));
        } catch (LionValuePraseErrorException e) {
            logger.error(e);
        }
        response.setCharacterEncoding("UTF-8");
        try {
            PrintWriter out = response.getWriter();
            out.write(String.valueOf(status));
        } catch (IOException e) {
            logger.warn(e);
        }
    }
}
