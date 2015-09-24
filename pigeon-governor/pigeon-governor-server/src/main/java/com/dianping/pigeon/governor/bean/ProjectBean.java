package com.dianping.pigeon.governor.bean;

import java.util.Date;

import com.dianping.pigeon.governor.model.Project;

/**
 * 
 * @author chenchongze
 *
 */
public class ProjectBean extends AJqGridBean{

	private String id;

    private String name;

    private Integer level;

    private String bu;

    private String owner;

    private String email;

    private String phone;

    private Date createtime;

    private Date modifytime;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getLevel() {
		return level;
	}

	public String getBu() {
		return bu;
	}

	public String getOwner() {
		return owner;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public Date getModifytime() {
		return modifytime;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public void setBu(String bu) {
		this.bu = bu;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public void setModifytime(Date modifytime) {
		this.modifytime = modifytime;
	}
    
	public Project createProject(){
		Project project = new Project();
		project.setName(this.name);
		project.setLevel(this.level);
		project.setBu(this.bu);
		project.setOwner(this.owner);
		project.setEmail(this.email);
		project.setPhone(this.phone);
		project.setCreatetime(createtime);
		project.setModifytime(this.modifytime);
		
		return project;
	}
	
	public Project convertToProject(){
		Project project = this.createProject();
		Integer id = Integer.parseInt(this.id);
		project.setId(id);
		
		return project;
	}
}
