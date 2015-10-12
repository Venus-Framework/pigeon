package com.dianping.pigeon.governor.bean;

import java.util.Date;

import com.dianping.pigeon.governor.model.Process;

public class ProcessBean {

	private String id;
	
	private String applicant;
	
	private Integer event;
	
	private String args;
	
	private Integer status;
	
	private Date createtime;

    private Date modifytime;

	public String getId() {
		return id;
	}

	public String getApplicant() {
		return applicant;
	}

	public Integer getEvent() {
		return event;
	}

	public String getArgs() {
		return args;
	}

	public Integer getStatus() {
		return status;
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

	public void setApplicant(String applicant) {
		this.applicant = applicant;
	}

	public void setEvent(Integer event) {
		this.event = event;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public void setModifytime(Date modifytime) {
		this.modifytime = modifytime;
	}
    
	public Process createProcess() {
		Process process = new Process();
		process.setApplicant(applicant);
		process.setEvent(event);
		process.setArgs(args);
		process.setStatus(status);
		process.setCreatetime(createtime);
		process.setModifytime(modifytime);
		
		return process;
	}
	
	public Process convertToProcess() {
		Process process = createProcess();
		Integer id = Integer.parseInt(this.id);
		process.setId(id);
		
		return process;
	}
}
