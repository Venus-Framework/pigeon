package com.dianping.pigeon.governor.bean;

public class CmdbBuBean {

	private String ci_type;
	
	private Integer _type;
	
	private String unique;
	
	private String bu_owner_email;
	
	private String bu_owner;
	
	private String bu_name;
	
	private String bu_owner_mobile;
	
	private Integer _id;

	public String getCi_type() {
		return ci_type;
	}

	public Integer get_type() {
		return _type;
	}

	public String getUnique() {
		return unique;
	}

	public String getBu_owner_email() {
		return bu_owner_email;
	}

	public String getBu_owner() {
		return bu_owner;
	}

	public String getBu_name() {
		return bu_name;
	}

	public String getBu_owner_mobile() {
		return bu_owner_mobile;
	}

	public Integer get_id() {
		return _id;
	}

	public void setCi_type(String ci_type) {
		this.ci_type = ci_type;
	}

	public void set_type(Integer _type) {
		this._type = _type;
	}

	public void setUnique(String unique) {
		this.unique = unique;
	}

	public void setBu_owner_email(String bu_owner_email) {
		this.bu_owner_email = bu_owner_email;
	}

	public void setBu_owner(String bu_owner) {
		this.bu_owner = bu_owner;
	}

	public void setBu_name(String bu_name) {
		this.bu_name = bu_name;
	}

	public void setBu_owner_mobile(String bu_owner_mobile) {
		this.bu_owner_mobile = bu_owner_mobile;
	}

	public void set_id(Integer _id) {
		this._id = _id;
	}

	@Override
	public String toString() {
		return "CmdbBuBean [ci_type=" + ci_type + ", _type=" + _type
				+ ", unique=" + unique + ", bu_owner_email=" + bu_owner_email
				+ ", bu_owner=" + bu_owner + ", bu_name=" + bu_name
				+ ", bu_owner_mobile=" + bu_owner_mobile + ", _id=" + _id + "]";
	}
	
	
}
