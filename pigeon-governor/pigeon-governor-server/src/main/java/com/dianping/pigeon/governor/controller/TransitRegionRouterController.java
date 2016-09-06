package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.exception.LionNullProjectException;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.RegionRouterService;
import com.dianping.pigeon.governor.util.UserRole;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by shihuashen on 16/8/16.
 */
@Controller
public class TransitRegionRouterController extends BaseController{


    @Autowired
    private ProjectService projectService;
    @Autowired
    private RegionRouterService regionRouterService;
    @Autowired
    private ProjectOwnerService projectOwnerService;


    @RequestMapping(value = {"/v2/region/projectRegionRouter"},method = RequestMethod.POST)
    public String ajaxLoadProjectRegionConfig(HttpServletRequest request , HttpServletResponse response , ModelMap modelMap){
        String projectName = request.getParameter("projectName");
        projectName = StringUtils.trim(projectName);
        if(projectName.equals("pigeon")){
            modelMap.put("error","Project :pigeon is an architecture component. The configure must be deployed in Lion");
            return "/v2/regionRouter/notFound";
        }
        if(projectService.findProject(projectName)!=null){
            System.out.println(projectName);
            boolean enableState ;
            try{
                enableState = regionRouterService.getEnableState(projectName);
            }catch(LionNullProjectException e){
                e.printStackTrace();
                modelMap.put("error","Project: "+projectName+" wasn't created in Lion");
                return "/v2/regionRouter/notFound";
            }
            System.out.println("The init region router state: "+ enableState);
            modelMap.put("projectName",projectName);
            commonnav(modelMap,request);
            User user = getUserInfo(request);
            String dpAccount = user!=null?user.getDpaccount():"null";
            System.out.println(user.getRoleid());
            if(UserRole.USER_SCM.getValue().equals(user.getRoleid()) ||
                    projectOwnerService.isProjectOwner(dpAccount,projectName)){
                modelMap.put("empowered","true");
                System.out.println("is Owner");
            }
            else{
                modelMap.put("empowered","false");
                System.out.println("not a Owner");
            }
            modelMap.put("enableState",String.valueOf(enableState));
            return "/v2/regionRouter/configSwitcher";
        }else{
            modelMap.put("error","Project: "+projectName+" doesn't exist .");
            return "/v2/regionRouter/notFound";
        }
    }
}
