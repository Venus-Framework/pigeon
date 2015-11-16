package com.dianping.pigeon.governor.util;

public enum UserRole {

	USER_SCM(1),
	
	USER_DEVELOPER(2);
	
	private Integer value;
	
	private UserRole(Integer value){
		this.value = value;
	}
	
	public Integer getValue(){
		return value;
	}
}
