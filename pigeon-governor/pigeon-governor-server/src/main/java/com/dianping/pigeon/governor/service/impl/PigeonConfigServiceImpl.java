package com.dianping.pigeon.governor.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.pigeon.governor.bean.ServiceBean;
import com.dianping.pigeon.governor.dao.ServiceMapper;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.service.PigeonConfigService;

@org.springframework.stereotype.Service
public class PigeonConfigServiceImpl implements PigeonConfigService {

	@Autowired
	private ServiceMapper serviceMapper;
	
	@Override
	public int create(ServiceBean serviceBean) {
		int sqlSucCount = -1;
		Service service = serviceBean.createService();
		
		if(StringUtils.isNotBlank(service.getName()) 
						&& service.getProjectid() != null
						&& service.getProjectid() > 0)
		{
			sqlSucCount = serviceMapper.insertSelective(service);
		}
		
		return sqlSucCount;
	}
	
	@Override
	public int deleteById(Integer id) {
		int sqlSucCount = -1;
		
		if(id > 0){
			sqlSucCount = serviceMapper.deleteByPrimaryKey(id);
		}
		
		return sqlSucCount;
	}
	
	@Override
	public int deleteByIdSplitByComma(String idsComma) {
		int sqlSucCount = 0;
		String idsArr[] = idsComma.split(",");
		
		for(String ids : idsArr){
			int id;
			int count = 0;
			
			try {
				id = Integer.parseInt(ids);
				count = serviceMapper.deleteByPrimaryKey(id);;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}finally{
				sqlSucCount += count;
			}
			
		}
		
		return sqlSucCount;
	}

	@Override
	public int updateById(ServiceBean serviceBean) {
		int sqlSucCount = -1;
		Service service = serviceBean.convertToService();
		
		if(StringUtils.isNotBlank(service.getName()) 
						&& service.getProjectid() != null
						&& service.getId() != null
						&& service.getProjectid() > 0
						&& service.getId() > 0)
		{
			sqlSucCount = serviceMapper.updateByPrimaryKey(service);
		}
		
		return sqlSucCount;
	}

	@Override
	public List<Service> retrieveAll() {
		return serviceMapper.selectByExample(null);
	}

	@Override
	public Service retrieveById(Integer id) {
		Service service = null;
		
		if(id > 0){
			service = serviceMapper.selectByPrimaryKey(id);
		}
		
		return service;
		
	}

}
