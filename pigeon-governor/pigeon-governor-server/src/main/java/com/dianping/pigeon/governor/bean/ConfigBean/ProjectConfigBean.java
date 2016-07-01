package com.dianping.pigeon.governor.bean.ConfigBean;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by shihuashen on 16/5/19.
 */
public class ProjectConfigBean {
    private String projectName;
    private List<ServiceConfigBean> services;
    public ProjectConfigBean(){
        this.services = new LinkedList<ServiceConfigBean>();
    }
    public void setServices(List<ServiceConfigBean> services){
        this.services.clear();
        this.services.addAll(services);
    }
    public void setProjectName(String projectName){
        this.projectName = projectName;
    }
    public String getProjectName(){
        return projectName;
    }
    public List<ServiceConfigBean> getServices(){
        return this.services;
    }
}
