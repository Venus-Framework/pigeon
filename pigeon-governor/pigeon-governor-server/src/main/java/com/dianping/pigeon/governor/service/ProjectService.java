package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.JqGridRespBean;
import com.dianping.pigeon.governor.bean.ProjectBean;
import com.dianping.pigeon.governor.model.Project;

public interface ProjectService {
	
	public int create(ProjectBean projectBean);
	
	public int deleteByIdSplitByComma(String idsComma);
	
	public int updateById(ProjectBean projectBean);

	public JqGridRespBean retrieveByJqGrid(int page, int rows);
	
	public Project findProject(String name);
	
	public Project createProject(String projectName, boolean fromCmdb);
}
