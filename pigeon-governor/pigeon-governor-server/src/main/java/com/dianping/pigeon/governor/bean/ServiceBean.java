package com.dianping.pigeon.governor.bean;

import com.dianping.pigeon.governor.model.Service;

public class ServiceBean {
	
	private Integer id;
	
	private String name;
	
	private String desc;
	
	private String group;

    private String hosts;

    private Integer projectid;

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public String getGroup() {
		return group;
	}

	public String getHosts() {
		return hosts;
	}

	public Integer getProjectid() {
		return projectid;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public void setProjectid(Integer projectid) {
		this.projectid = projectid;
	}

	public Service createService(){
		Service service = new Service();
		
		service.setName(this.name);
		service.setDesc(this.desc);
		service.setGroup(this.group);
		service.setHosts(this.hosts);
		service.setProjectid(this.projectid);
		
		return service;
	}

	public Service convertToService(){
		Service service = this.createService();
		service.setId(this.id);
		
		return service;
	}
}
