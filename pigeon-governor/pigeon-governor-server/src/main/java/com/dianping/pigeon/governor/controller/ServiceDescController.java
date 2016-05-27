package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.scanServiceDesc.ScanServiceTask;
import com.dianping.pigeon.governor.bean.scanServiceDesc.ScanStaticsBean;
import com.dianping.pigeon.governor.bean.serviceDesc.SearchResultBean;
import com.dianping.pigeon.governor.bean.serviceDesc.ServiceDescBean;
import com.dianping.pigeon.governor.bean.serviceDesc.ServiceDescCache;
import com.dianping.pigeon.governor.service.DescSearchService;
import com.dianping.pigeon.governor.service.ServiceDescService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
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
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shihuashen on 16/4/21.
 */
@Controller
public class ServiceDescController extends BaseController{
    @Autowired
    private ServiceDescService serviceDescService;
    @Autowired
    private DescSearchService descSearchService;
    @Autowired
    private ScanServiceTask task;
    private Gson gson = new GsonBuilder().create();
    private Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
    private JsonParser jp = new JsonParser();



    private String prettyPrint(Object object){
        return gsonPretty.toJson(jp.parse(gson.toJson(object)));
    }

    @RequestMapping(value = {"/doc/{serviceId}"},method = RequestMethod.GET)
    public String testCombinedMapper(ModelMap modelMap,
                                     HttpServletRequest request,
                                     @PathVariable final Integer serviceId,
                                     HttpServletResponse response){
        commonnav(modelMap, request);
        ServiceDescBean serviceDescBean = serviceDescService.getServiceDescBeanById(serviceId);
        if(serviceDescBean!=null){
            modelMap.put("serviceDescBean", serviceDescService.getServiceDescBeanById(serviceId));
            modelMap.addAllAttributes(serviceDescService.getServiceMetaInfoById(serviceId));
            return "/doc/serviceDoc";
        }else{
            return "/doc/emptyDoc";
        }
    }

    @RequestMapping(value={"/doc/desc/{serviceId}"},method = RequestMethod.POST)
    public void ajaxUpdateServiceDesc(ModelMap modelMap,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable final Integer serviceId){
        commonnav(modelMap, request);
        String value =request.getParameter("value");
        System.out.println(value);
        serviceDescService.updateServiceDescById(serviceId,value);
    }
    @RequestMapping(value = {"/doc/method/desc/{methodId}"},method = RequestMethod.POST)
    public void ajaxUpdateMethodDesc(ModelMap modelMap,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable final Integer methodId){
        commonnav(modelMap, request);
        String value = request.getParameter("value");
        System.out.println(value);
        serviceDescService.updateMethodDescById(methodId,value);
    }
    @RequestMapping(value={"/doc/param/desc/{paramId}"},method = RequestMethod.POST)
    public void ajaxUpdateParamDesc(ModelMap modelMap,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    @PathVariable final Integer paramId){
        commonnav(modelMap, request);
        String value = request.getParameter("value");
        System.out.println(value);
        serviceDescService.updateParamDescById(paramId,value);
    }
    @RequestMapping(value={"/doc/exception/desc/{exceptionId}"},method = RequestMethod.POST)
    public void ajaxUpdateExceptionDesc(ModelMap modelMap,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        @PathVariable final Integer exceptionId){
        commonnav(modelMap, request);
        String value = request.getParameter("value");
        System.out.println(value);
        serviceDescService.updateExceptionDescById(exceptionId,value);
    }
    @RequestMapping(value = {"/doc/scanJson"},method = RequestMethod.GET)
    public String testGetJson(ModelMap modelMap,HttpServletRequest request,HttpServletResponse response){
        ScanStaticsBean scanStaticsBean = task.startScanJson();
        response.setContentType("text/html;charset=UTF-8");
        modelMap.put("statics",scanStaticsBean);
        return "/doc/scanResult";
    }
    @RequestMapping(value = {"/doc/search"},method = RequestMethod.POST)
    public void testGetSearch(HttpServletRequest request,
                              HttpServletResponse response,
                              ModelMap modelMap){
        commonnav(modelMap, request);
        Gson gson = new Gson();
        long startTime = System.currentTimeMillis();
        String searchKey = request.getParameter("keyword");
        System.out.println(searchKey);
        SearchResultBean ans = descSearchService.searchForViewBean(searchKey);
        ans.setConsumeTime(System.currentTimeMillis()-startTime);
        ans.setKeyWord(searchKey);
        try {
            PrintWriter pw = response.getWriter();
            pw.write(gson.toJson(ans));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ;
    }

    @RequestMapping(value = {"/doc/docCenter"},method = RequestMethod.GET)
    public String testSearchCenter(HttpServletRequest request,
                                  HttpServletResponse response,
                                   ModelMap modelMap){
        commonnav(modelMap, request);
        return "/doc/search";
    }
}
