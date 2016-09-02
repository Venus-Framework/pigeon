package com.dianping.pigeon.governor.controller.v3;

import com.dianping.pigeon.governor.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by shihuashen on 16/9/2.
 */
@Controller
public class FrameworkController extends BaseController{
    @RequestMapping(value = {"/framework/doc"},method = RequestMethod.GET)
    public String docPage(HttpServletRequest request,
                          HttpServletResponse response,
                          ModelMap modelMap){
        commonnav(modelMap, request);
        return "/v3/serviceDoc/esSearch";
    }

}
