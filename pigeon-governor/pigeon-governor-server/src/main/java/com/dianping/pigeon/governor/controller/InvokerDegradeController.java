package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.degrade.DegradeConfig;
import com.dianping.pigeon.governor.exception.LionNullProjectException;
import com.dianping.pigeon.governor.model.OpLog;
import com.dianping.pigeon.governor.model.ServiceNode;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.*;
import com.dianping.pigeon.governor.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by shihuashen on 16/8/17.
 */
@Controller
public class InvokerDegradeController extends BaseController{
    @Autowired
    private InvokerDegradeService invokerDegradeService;
    @Autowired
    private ProjectOwnerService projectOwnerService;
    @Autowired
    private ServiceNodeService serviceNodeService;
    @Autowired
    private OpLogService opLogService;
    @Autowired
    private ProjectService projectService;


    private Logger logger = LogManager.getLogger(InvokerDegradeController.class.getName());
    @RequestMapping(value = {"/config/degrade"},method = RequestMethod.POST)
    public String main(HttpServletRequest request,
                     HttpServletResponse response,
                     ModelMap modelMap){
        commonnav(modelMap,request);
        User user = getUserInfo(request);
        GsonUtils.Print(user);
        String dpAccount = user!=null?user.getDpaccount():"null";
        String projectName = request.getParameter("projectName");
        boolean switcherEnable = false;
        boolean forceState;
        boolean autoState;
        boolean failureState;
        double recoverPercentage;
        if(UserRole.USER_SCM.getValue().equals(user.getRoleid()) ||
                projectOwnerService.isProjectOwner(dpAccount,projectName))
            switcherEnable = true;
        try {
            forceState = invokerDegradeService.getForceDegradeState(projectName);
            autoState = invokerDegradeService.getAutoDegradeState(projectName);
            failureState = invokerDegradeService.getFailureDegradeState(projectName);
            recoverPercentage = invokerDegradeService.getRecoverPercentage(projectName);
        } catch (LionNullProjectException e) {
            logger.info(e);
            modelMap.put("error","Project: "+projectName+" wasn't created in Lion.");
            return "/config/project/LionLack";
        }
        modelMap.put("forceState",forceState);
        modelMap.put("autoState",autoState);
        modelMap.put("failureState",failureState);
        modelMap.put("switcherEnable",switcherEnable);
        modelMap.put("recoverPercentage",recoverPercentage);
        return "/v2/degrade/main";
    }
    @RequestMapping(value={"/degrade/failure/enable"},method = RequestMethod.POST)
    public void enableFailure(HttpServletRequest request,
                              HttpServletResponse response,
                              ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        boolean status = invokerDegradeService.setFailureDegradeState(projectName,"true");
        logSwitch(request,projectName,"failure switcher","turn on ",status);
    }
    @RequestMapping(value={"/degrade/failure/disable"},method = RequestMethod.POST)
    public void disableFailure(HttpServletRequest request,
                               HttpServletResponse response,
                               ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        boolean status = invokerDegradeService.setFailureDegradeState(projectName,"false");
        logSwitch(request,projectName,"failure switcher","turn off ",status);
    }



    @RequestMapping(value = {"/degrade/force/enable"}, method = RequestMethod.POST)
    public void enableForce(HttpServletRequest request,
                            HttpServletResponse response,
                            ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        boolean status = invokerDegradeService.setForceDegradeState(projectName,"true");
        logSwitch(request,projectName,"force switcher","turn on ",status);
    }

    @RequestMapping(value = {"/degrade/force/disable"},method = RequestMethod.POST)
    public void disableForce(HttpServletRequest request,
                             HttpServletResponse response,
                             ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        System.out.println(projectName);
        boolean status = invokerDegradeService.setForceDegradeState(projectName,"false");
        logSwitch(request,projectName,"force switcher","turn off ",status);
    }

    @RequestMapping(value={"/degrade/auto/enable"},method = RequestMethod.POST)
    public void enableAuto(HttpServletRequest request,
                           HttpServletResponse response,
                           ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        boolean status = invokerDegradeService.setAutoDegradeState(projectName,"true");
        logSwitch(request,projectName,"auto switcher","turn on ",status);
    }

    @RequestMapping(value={"/degrade/auto/disable"},method = RequestMethod.POST)
    public void disableAuto(HttpServletRequest request,
                            HttpServletResponse response,
                            ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        boolean status = invokerDegradeService.setAutoDegradeState(projectName,"false");
        logSwitch(request,projectName,"auto switcher","turn off ",status);
    }
    @RequestMapping(value={"/degrade/recover/set"},method = RequestMethod.POST)
    public void setRecoverPercentage(HttpServletRequest request,
                                     HttpServletResponse response,
                                     ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        String value = request.getParameter("value");
        System.out.println(projectName);
        System.out.println(value);
        boolean setStatus =  invokerDegradeService.setRecoverPercentage(projectName,Double.valueOf(value));
        logRecoverSet(request,projectName,Double.valueOf(value),setStatus);
        response.setCharacterEncoding("UTF-8");
        try {
            PrintWriter out = response.getWriter();
            out.write(String.valueOf(setStatus));
        } catch (IOException e) {
            logger.warn(e);
        }
    }


    @RequestMapping(value={"/degrade/configs"},method = RequestMethod.POST)
    public String getAllConfigs(HttpServletRequest request,
                                HttpServletResponse response,
                                ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        List<DegradeConfig> configs = invokerDegradeService.getDegradeConfigs(projectName);
        modelMap.put("configs",configs);
        User user = getUserInfo(request);
        List<ServiceNode> serviceNodes = serviceNodeService.retrieveAllIdNamesByCache();
        HashSet<String> set = new HashSet<String>();
        List<ServiceNode> serviceNames = new LinkedList<ServiceNode>();
        for(ServiceNode serviceNode : serviceNodes){
            String serviceName = serviceNode.getServiceName();
            if(!set.contains(serviceName)){
                set.add(serviceName);
                serviceNames.add(serviceNode);
            }
        }
        if(UserRole.USER_SCM.getValue().equals(user.getRoleid()) ||
                projectOwnerService.isProjectOwner(user.getDpaccount(),projectName)){
            modelMap.put("serviceNames",GsonUtils.toJson(serviceNames));
            return "/v2/degrade/configs-test";
        }else
            return "/v2/degrade/readOnlyConfigs";
    }




    @RequestMapping(value={"/config/degrade/add"},method = RequestMethod.POST)
    public void addDegradeConfig(HttpServletRequest request,
                                 HttpServletResponse response,
                                 ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        String serviceName = request.getParameter("serviceName");
        String methodName = request.getParameter("methodName");
        String returnValue = request.getParameter("value");
        DegradeConfig degradeConfig = new DegradeConfig();
        degradeConfig.setProjectName(projectName);
        degradeConfig.setMethodName(methodName);
        degradeConfig.setServiceName(serviceName);
        degradeConfig.setReturnValue(returnValue);
        boolean addStatus = invokerDegradeService.addDegradeConfig(degradeConfig);
        log(request,degradeConfig,"add",addStatus);
        response.setCharacterEncoding("UTF-8");
        try {
            PrintWriter out = response.getWriter();
            out.write(String.valueOf(addStatus));
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    @RequestMapping(value={"/config/degrade/update"},method = RequestMethod.POST)
    public void updateDegradeConfig(HttpServletRequest request,
                                    HttpServletResponse response,
                                    ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        String serviceName = request.getParameter("serviceName");
        String methodName = request.getParameter("methodName");
        String returnValue = request.getParameter("value");
        DegradeConfig degradeConfig = new DegradeConfig();
        degradeConfig.setProjectName(projectName);
        degradeConfig.setMethodName(methodName);
        degradeConfig.setServiceName(serviceName);
        degradeConfig.setReturnValue(returnValue);
        boolean updateStatus = invokerDegradeService.updateDegradeConfig(degradeConfig);
        log(request,degradeConfig,"update",updateStatus);
        try {
            PrintWriter out = response.getWriter();
            out.write(String.valueOf(updateStatus));
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    @RequestMapping(value = {"/config/degrade/delete"},method = RequestMethod.POST)
    public void deleteDegradeConfig(HttpServletRequest request,
                                    HttpServletResponse response,
                                    ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        String serviceName = request.getParameter("serviceName");
        String methodName = request.getParameter("methodName");
        DegradeConfig degradeConfig = new DegradeConfig();
        degradeConfig.setProjectName(projectName);
        degradeConfig.setMethodName(methodName);
        degradeConfig.setServiceName(serviceName);
        boolean deleteStatus = invokerDegradeService.deleteDegradeConfig(degradeConfig);
        log(request,degradeConfig,"delete",deleteStatus);
        try {
            PrintWriter out = response.getWriter();
            out.write(String.valueOf(deleteStatus));
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    private Future<Integer> log(HttpServletRequest request,DegradeConfig config,String configOp,boolean status){
        OpLog opLog = new OpLog();
        User user = getUserInfo(request);
        String currentUser = user.getDpaccount();
        int projectId = projectService.findProject(config.getProjectName()).getId();
        String reqIp = IPUtils.getUserIP(request);
        OpType opType = OpType.SERVICE_DEGRADE;
        opLog.setDpaccount(currentUser);
        opLog.setProjectid(projectId);
        opLog.setReqip(reqIp);
        opLog.setOptime(new Date());
        opLog.setContent("Degrade "+configOp+" with the user defined config :"+GsonUtils.toJson(config)+" The op return status is "+status);
        opLog.setOptype(opType.getValue());
        return opLogService.asyncCreate(opLog);
    }
    private Future<Integer> logSwitch(HttpServletRequest request,String projectName, String switcherName, String switchOp,boolean setStatus){
        OpLog opLog = new OpLog();
        User user = getUserInfo(request);
        String currentUser = user.getDpaccount();
        int projectId = projectService.findProject(projectName).getId();
        String reqIp = IPUtils.getUserIP(request);
        OpType opType = OpType.SERVICE_DEGRADE;
        opLog.setDpaccount(currentUser);
        opLog.setProjectid(projectId);
        opLog.setReqip(reqIp);
        opLog.setOptime(new Date());
        opLog.setContent("Degrade "+switchOp+" "+switcherName+"# The op return status is "+setStatus);
        opLog.setOptype(opType.getValue());
        return opLogService.asyncCreate(opLog);
    }
    private Future<Integer> logRecoverSet(HttpServletRequest request,String projectName,double value,boolean setStatus){
        OpLog opLog = new OpLog();
        User user = getUserInfo(request);
        String currentUser = user.getDpaccount();
        int projectId = projectService.findProject(projectName).getId();
        String reqIp = IPUtils.getUserIP(request);
        OpType opType = OpType.SERVICE_DEGRADE;
        opLog.setDpaccount(currentUser);
        opLog.setProjectid(projectId);
        opLog.setReqip(reqIp);
        opLog.setOptime(new Date());
        opLog.setContent("Set recover percentage to"+value+"# The op return status is "+setStatus);
        opLog.setOptype(opType.getValue());
        return opLogService.asyncCreate(opLog);
    }
}
