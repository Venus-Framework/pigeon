package com.dianping.pigeon.governor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by shihuashen on 16/7/29.
 */
@Controller
public class MessageController extends BaseController{
    @RequestMapping(value = {"/message"},method = RequestMethod.GET)
    public String mainEntrance(HttpServletRequest request,
                               HttpServletResponse response,
                               ModelMap modelMap){
        commonnav(modelMap,request);
        return "/message/main";
    }
}
