package com.dianping.pigeon.governor.service.impl;

import com.dianping.pigeon.governor.dao.CustomOrgMapper;
import com.dianping.pigeon.governor.dao.ProjectOrgMapper;
import com.dianping.pigeon.governor.dao.ServiceNodeMapper;
import com.dianping.pigeon.governor.model.ProjectOrg;
import com.dianping.pigeon.governor.model.ServiceNode;
import com.dianping.pigeon.governor.model.ServiceNodeExample;
import com.dianping.pigeon.governor.service.DBSearchService;
import com.google.gson.JsonArray;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by shihuashen on 16/8/29.
 */
@Service
public class DBSearchServiceImpl implements DBSearchService{
    private List<String> prefixs ;
    @Autowired
    private CustomOrgMapper customOrgMapper;
    @Autowired
    private ServiceNodeMapper serviceNodeMapper;
    @PostConstruct
    private void init(){
        this.prefixs = new LinkedList<String>();
        this.prefixs.add("http://service.dianping.com/");
        this.prefixs.add("com.dianping.");
        this.prefixs.add("com.sankuai.");
    }

    @Override
    public JSONArray getProjectTypeAheadInfo() {
        JSONArray projects = new JSONArray();
        List<ProjectOrg> projectOrgs = customOrgMapper.getAllProjectOrg();
        for(ProjectOrg projectOrg : projectOrgs){
            JSONObject project = new JSONObject();
            project.put("name",projectOrg.getName());
            project.put("product",projectOrg.getProduct());
            project.put("bu",projectOrg.getBu());
            projects.put(project);
        }
        return projects;
    }

    @Override
    public JSONArray getServiceTypeAheadInfo() {
        ServiceNodeExample serviceNodeExample = new ServiceNodeExample();
        serviceNodeExample.createCriteria();
        List<ServiceNode> serviceNodes = new ArrayList<ServiceNode>();
        try {
            serviceNodes = serviceNodeMapper.selectByExample(serviceNodeExample);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        Map<String,HashSet<String>> serviceMeta = new HashMap<String, HashSet<String>>();
        for(ServiceNode serviceNode :serviceNodes){
            String serviceName = serviceNode.getServiceName();
            String projectName = serviceNode.getProjectName();
            if(serviceMeta.containsKey(serviceName)){
                HashSet<String> set = serviceMeta.get(serviceName);
                set.add(projectName);
            }else {
                HashSet<String> set = new HashSet<String>();
                set.add(projectName);
                serviceMeta.put(serviceName,set);
            }
        }
        JSONArray services = new JSONArray();
        for(String serviceName : serviceMeta.keySet()){
            HashSet<String> set = serviceMeta.get(serviceName);
            for (String projectName : set){
                services.put(formatServiceJSON(serviceName,projectName));
            }
        }
        return services;
    }

    private JSONObject formatServiceJSON(String serviceName,String projectName){
        JSONObject service = new JSONObject();
        service.put("name",serviceName);
        service.put("projectName",projectName);
        service.put("displayName",serviceName);
        service.put("prefix","");
        for(String prefix : this.prefixs){
            if(serviceName.startsWith(prefix)){
                service.put("displayName",serviceName.substring(prefix.length()));
                service.put("prefix",prefix);
            }
        }
        return service;
    }

}
