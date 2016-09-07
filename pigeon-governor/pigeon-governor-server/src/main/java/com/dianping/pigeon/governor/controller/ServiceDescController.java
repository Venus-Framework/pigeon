package com.dianping.pigeon.governor.controller;

import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.bean.scanServiceDesc.PurgeExpireDescTask;
import com.dianping.pigeon.governor.bean.scanServiceDesc.ScanServiceTask;
import com.dianping.pigeon.governor.bean.scanServiceDesc.ScanStaticsBean;
import com.dianping.pigeon.governor.bean.serviceDesc.SearchResults;
import com.dianping.pigeon.governor.bean.serviceDesc.ServiceDescBean;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.EsService;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ServiceDescService;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.UserRole;
import com.google.gson.Gson;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHits;
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
import java.util.Map;

/**
 * Created by shihuashen on 16/4/21.
 */
@Controller
public class ServiceDescController extends BaseController{
    @Autowired
    private ServiceDescService serviceDescService;

    @Autowired
    private ScanServiceTask task;

    @Autowired
    private EsService esService;

    @Autowired
    private PurgeExpireDescTask purgeExpireDescTask;
    @Autowired
    private ProjectOwnerService projectOwnerService;


    private String index = Lion.get("pigeon-governor-server.es.index","bean");
    private String type = Lion.get("pigeon-governor-server.es.type","camel");


    @RequestMapping(value = {"/doc/{serviceId}"},method = RequestMethod.GET)
    public String CombinedMapper(ModelMap modelMap,
                                     HttpServletRequest request,
                                     @PathVariable final Integer serviceId,
                                     HttpServletResponse response){
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
    public void search(HttpServletRequest request,
                              HttpServletResponse response,
                              ModelMap modelMap){
        commonnav(modelMap, request);
        String searchKey = request.getParameter("searchKey");
        System.out.println(searchKey);
        try{
            SearchResponse countResponse  = esService.countTotalHits(index,type,searchKey);
            SearchResponse searchResponse= esService.search(index,type,searchKey,0,10);
            SearchHits searchHits = searchResponse.getHits();
            SearchResults results = new SearchResults(searchHits);
            results.setCount(countResponse.getHits().getTotalHits());
            results.setTimeConsume(searchResponse.getTookInMillis());
            PrintWriter pw = response.getWriter();
            pw.write(new Gson().toJson(results));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = {"/doc/paginationSearch"},method = RequestMethod.POST)
    public void paginationSearch(HttpServletRequest request,
                                 HttpServletResponse response,
                                 ModelMap modelMap){
        String searchKey  = request.getParameter("searchKey");
        int start = Integer.parseInt(request.getParameter("start"));
        int size = Integer.parseInt(request.getParameter("size"));
        try{
            SearchResponse searchResponse = esService.search(index,type,searchKey,start,size);
            SearchHits searchHits = searchResponse.getHits();
            SearchResults result = new SearchResults(searchHits);
            result.setTimeConsume(searchResponse.getTookInMillis());
            PrintWriter pw = response.getWriter();
            pw.write(new Gson().toJson(result));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = {"/doc"},method = RequestMethod.GET)
    public String searchCenter(HttpServletRequest request,
                                  HttpServletResponse response,
                                   ModelMap modelMap){
        commonnav(modelMap, request);
        return "/v2/serviceDoc/esSearch";
    }


    @RequestMapping(value={"/doc/Purge"},method = RequestMethod.GET)
    public void test11(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        User user = (User) request.getSession().getAttribute(Constants.DP_USER);
        String currentUser = user!=null?user.getDpaccount():"";
        if(currentUser.equals("shihuashen"))
            purgeExpireDescTask.startPurge();
    }
}
