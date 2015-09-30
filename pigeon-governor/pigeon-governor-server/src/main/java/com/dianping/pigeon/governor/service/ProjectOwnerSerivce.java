package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.model.Project;

public interface ProjectOwnerSerivce {

	public boolean isProjectOwner(String dpaccount, String projectName);
	
	public boolean isProjectOwner(String dpaccount, Project project);
	
}
