package com.dianping.pigeon.governor.bean;

import java.util.ArrayList;
import java.util.List;

public class ServiceRetrieveFilters {

	private String groupOp;
	
	private List<ServiceRetrieveFiltersRule> rules;
	
	public ServiceRetrieveFilters(){
		rules = new ArrayList<ServiceRetrieveFiltersRule>();
	}
	
	public String getGroupOp() {
		return groupOp;
	}

	public List<ServiceRetrieveFiltersRule> getRules() {
		return rules;
	}

	public void setGroupOp(String groupOp) {
		this.groupOp = groupOp;
	}

	public void setRules(List<ServiceRetrieveFiltersRule> rules) {
		this.rules = rules;
	}

}
