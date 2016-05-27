package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.service.ProjectService;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by shihuashen on 16/4/6.
 */
@Controller
public class ConfigureController {
    @Autowired
    private ProjectService projectService;
    @RequestMapping(value = {"/configure"}, method = RequestMethod.GET)
    public String configureMainPage(HttpServletRequest req, HttpServletResponse res){
        return "/projects/configure";

    }
}
