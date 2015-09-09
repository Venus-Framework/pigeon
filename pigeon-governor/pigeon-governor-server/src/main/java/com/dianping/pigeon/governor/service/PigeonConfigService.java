package com.dianping.pigeon.governor.service;

import java.util.List;

import com.dianping.pigeon.governor.bean.ServiceBean;
import com.dianping.pigeon.governor.bean.JqGridTableBean;
import com.dianping.pigeon.governor.model.Service;

public interface PigeonConfigService {

	public int create(ServiceBean serviceBean);
	
	public int deleteById(Integer id);
	
	public int deleteByIdSplitByComma(String idsComma);
	
	public int updateById(ServiceBean serviceBean);
	
	
	public List<Service> retrieveAll();
	
	public Service retrieveById(Integer id);
	
	public List<Service> retrieveByPageAndRows(int page, int rows);
	
	public JqGridTableBean retrieveByJqGrid(int page, int rows);
	
}
