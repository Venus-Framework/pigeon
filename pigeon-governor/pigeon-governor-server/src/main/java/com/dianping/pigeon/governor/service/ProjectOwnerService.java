package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.model.Project;

public interface ProjectOwnerService {

	public boolean isProjectOwner(String dpaccount, String projectName);

	public boolean isProjectOwner(String dpaccount, Project project);
	
	public void createDefaultOwner(String email, String projectName);

	public void create(Integer userId, Integer projectId);
	
}
