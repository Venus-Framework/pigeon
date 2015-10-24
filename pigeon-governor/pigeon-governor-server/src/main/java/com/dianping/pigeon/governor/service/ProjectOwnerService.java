package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.User;

public interface ProjectOwnerService {

	public boolean isProjectOwner(String dpaccount, String projectName);
	
	public boolean isProjectOwner(String dpaccount, Project project);
	
	public void createDefaultOwner(String email);

	public void create(User user, Project project);
	
}
