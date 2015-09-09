package com.dianping.pigeon.governor.bean;

import java.util.ArrayList;
import java.util.List;

public class JqGridReqFilters {

	private String groupOp;
	
	private List<JqGridReqFiltersRule> rules;
	
	public JqGridReqFilters(){
		rules = new ArrayList<JqGridReqFiltersRule>();
	}
	
	public String getGroupOp() {
		return groupOp;
	}

	public List<JqGridReqFiltersRule> getRules() {
		return rules;
	}

	public void setGroupOp(String groupOp) {
		this.groupOp = groupOp;
	}

	public void setRules(List<JqGridReqFiltersRule> rules) {
		this.rules = rules;
	}

}
