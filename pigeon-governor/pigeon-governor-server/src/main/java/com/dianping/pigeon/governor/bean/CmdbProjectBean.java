package com.dianping.pigeon.governor.bean;

import java.util.List;

public class CmdbProjectBean {

	private String ci_type;
	
	private Integer _type;
	
    private String project_name; 
    
    private Integer project_level;
    
    private String backup_rd;
    
    private List<String> beta_deploy_owner;
    
    private String project_description;
    
    private String container_type;
    
    private String project_type;
    
    private String unique;
    
    private String qa_duty;
    
    private Integer _id;
    
    private String op_email;
    
    private String project_status;
    
    private String project_email;
    
    private String rd_mobile;
    
    private String rd_duty; 
    
    private String op_duty;
    
    private String op_mobile;

	public String getCi_type() {
		return ci_type;
	}

	public Integer get_type() {
		return _type;
	}

	public String getProject_name() {
		return project_name;
	}

	public Integer getProject_level() {
		return project_level;
	}

	public String getBackup_rd() {
		return backup_rd;
	}

	public List<String> getBeta_deploy_owner() {
		return beta_deploy_owner;
	}

	public String getProject_description() {
		return project_description;
	}

	public String getContainer_type() {
		return container_type;
	}

	public String getProject_type() {
		return project_type;
	}

	public String getUnique() {
		return unique;
	}

	public String getQa_duty() {
		return qa_duty;
	}

	public Integer get_id() {
		return _id;
	}

	public String getOp_email() {
		return op_email;
	}

	public String getProject_status() {
		return project_status;
	}

	public String getProject_email() {
		return project_email;
	}

	public String getRd_mobile() {
		return rd_mobile;
	}

	public String getRd_duty() {
		return rd_duty;
	}

	public String getOp_duty() {
		return op_duty;
	}

	public String getOp_mobile() {
		return op_mobile;
	}

	public void setCi_type(String ci_type) {
		this.ci_type = ci_type;
	}

	public void set_type(Integer _type) {
		this._type = _type;
	}

	public void setProject_name(String project_name) {
		this.project_name = project_name;
	}

	public void setProject_level(Integer project_level) {
		this.project_level = project_level;
	}

	public void setBackup_rd(String backup_rd) {
		this.backup_rd = backup_rd;
	}

	public void setBeta_deploy_owner(List<String> beta_deploy_owner) {
		this.beta_deploy_owner = beta_deploy_owner;
	}

	public void setProject_description(String project_description) {
		this.project_description = project_description;
	}

	public void setContainer_type(String container_type) {
		this.container_type = container_type;
	}

	public void setProject_type(String project_type) {
		this.project_type = project_type;
	}

	public void setUnique(String unique) {
		this.unique = unique;
	}

	public void setQa_duty(String qa_duty) {
		this.qa_duty = qa_duty;
	}

	public void set_id(Integer _id) {
		this._id = _id;
	}

	public void setOp_email(String op_email) {
		this.op_email = op_email;
	}

	public void setProject_status(String project_status) {
		this.project_status = project_status;
	}

	public void setProject_email(String project_email) {
		this.project_email = project_email;
	}

	public void setRd_mobile(String rd_mobile) {
		this.rd_mobile = rd_mobile;
	}

	public void setRd_duty(String rd_duty) {
		this.rd_duty = rd_duty;
	}

	public void setOp_duty(String op_duty) {
		this.op_duty = op_duty;
	}

	public void setOp_mobile(String op_mobile) {
		this.op_mobile = op_mobile;
	}

	@Override
	public String toString() {
		return "CmdbProjectBean [ci_type=" + ci_type + ", _type=" + _type
				+ ", project_name=" + project_name + ", project_level="
				+ project_level + ", backup_rd=" + backup_rd
				+ ", beta_deploy_owner=" + beta_deploy_owner
				+ ", project_description=" + project_description
				+ ", container_type=" + container_type + ", project_type="
				+ project_type + ", unique=" + unique + ", qa_duty=" + qa_duty
				+ ", _id=" + _id + ", op_email=" + op_email
				+ ", project_status=" + project_status + ", project_email="
				+ project_email + ", rd_mobile=" + rd_mobile + ", rd_duty="
				+ rd_duty + ", op_duty=" + op_duty + ", op_mobile=" + op_mobile
				+ "]";
	}
    
}
