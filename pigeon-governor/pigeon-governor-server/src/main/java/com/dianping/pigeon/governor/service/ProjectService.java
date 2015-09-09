package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.JqGridRespBean;
import com.dianping.pigeon.governor.bean.ProjectBean;

public interface ProjectService {
	
	public int create(ProjectBean projectBean);
	
	public int deleteByIdSplitByComma(String idsComma);
	
	public int updateById(ProjectBean projectBean);

	public JqGridRespBean retrieveByJqGrid(int page, int rows);
}
