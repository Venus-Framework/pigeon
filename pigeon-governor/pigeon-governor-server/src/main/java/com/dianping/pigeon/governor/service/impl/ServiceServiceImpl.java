package com.dianping.pigeon.governor.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.pigeon.governor.bean.JqGridRespBean;
import com.dianping.pigeon.governor.bean.ServiceBean;
import com.dianping.pigeon.governor.dao.ServiceMapper;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.model.ServiceExample;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.ServiceService;
import com.dianping.pigeon.governor.util.IPUtils;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.common.util.Constants;

/**
 * 
 * @author chenchongze
 *
 */
@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {

	@Autowired
	private ServiceMapper serviceMapper;
	@Autowired
	private ProjectService projectService;
	
	@Override
	public int create(ServiceBean serviceBean) {
		Service service = serviceBean.createService();
		
		return create(service);
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
		Service service = serviceBean.convertToService();
		
		return updateById(service);
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

	@Override
	public List<Service> retrieveByPageAndRows(int page, int rows) {
		List<Service> services = null;
		
		if(page > 0){
			services = serviceMapper.selectByPageAndRows((page - 1) * rows, rows);
		}
		
		return services;
	}

	@Override
	public JqGridRespBean retrieveByJqGrid(int page, int rows) {
		JqGridRespBean jqGridTableBean = null;
		
		if(page > 0){
			List<Service> services = serviceMapper.selectByPageAndRows((page - 1) * rows, rows);
			int totalRecords = serviceMapper.countByExample(null);
			int totalPages = (totalRecords - 1) / rows + 1;
			
			jqGridTableBean = new JqGridRespBean();
			jqGridTableBean.setData(services);
			jqGridTableBean.setCurrentPage(page);
			jqGridTableBean.setTotalRecords(totalRecords);
			jqGridTableBean.setTotalPages(totalPages);
		}
		
		return jqGridTableBean;
	}

	@Override
	public List<Service> getServiceList(int projectId) {
		ServiceExample example = new ServiceExample();
		example.createCriteria().andProjectidEqualTo(projectId);
		List<Service> services = serviceMapper.selectByExample(example);
		
		return services;
	}

	@Override
	public Service getService(String name, String group) {
		ServiceExample example = new ServiceExample();
		example.createCriteria().andNameEqualTo(name).andGroupEqualTo(group);
		List<Service> services = serviceMapper.selectByExample(example);
		
		if(services != null){
			
			if(services.size() > 0){
				
				return services.get(0);
				
			}
		}
		
		return null;
	}

	@Override
	public int updateById(Service service) {
		int sqlSucCount = -1;
		
		if(StringUtils.isNotBlank(service.getName()) 
						&& service.getProjectid() != null
						&& service.getId() != null)
		{
			sqlSucCount = serviceMapper.updateByPrimaryKeySelective(service);
		}
		
		return sqlSucCount;
	}

	@Override
	public void registryUpdateService(Service oriService, Service newService) throws RegistryException {
		RegistryManager.getInstance().setServerService(
				newService.getName(),newService.getGroup(),newService.getHosts());
		
		Set<String> oriHostSet = new HashSet<String>();
        for(String host : oriService)
            oriHostSet.add(host);
        
        Set<String> hostSet = new HashSet<String>();
        for(String host : newService)
            hostSet.add(host);
        
        Collection<String> addSet = CollectionUtils.subtract(hostSet, oriHostSet);
        for(String host : addSet) {
        	RegistryManager.getInstance().setServerWeight(host, Constants.WEIGHT_DEFAULT);
        }
	}

	@Override
	public String publishService(String project,String service, String group, 
									String ip, String port, String updatezk) throws RegistryException {
		
		boolean writeZk = "true".equalsIgnoreCase(updatezk);
	    Service oriService = getService(service, group);
	    Service newService = null;
	    
	    if(oriService != null) {
	    	newService = new Service(oriService);
	    	newService.setHosts(IPUtils.addHost(newService.getHosts(), ip, port));
	        updateById(newService);
	        
	        if(writeZk)
	        	registryUpdateService(oriService, newService);
	        
	    } else {
	    	
	        Project newProject = projectService.createProject(project, true);
	        
	        if(newProject == null){
	        	return null;
	        }
	        
	        newService = new Service();
	        newService.setProjectid(newProject.getId());
	        newService.setName(service);
	        newService.setGroup(group);
	        newService.setHosts(IPUtils.addHost(null, ip, port));
	        create(newService);
	        
	        if(writeZk)
	        	registryCreateService(newService);
	        
	    }
	    
	    return newService.getHosts();
	}

	@Override
	public int create(Service service) {
		int sqlSucCount = -1;
		
		if(StringUtils.isNotBlank(service.getName()) 
						&& service.getProjectid() != null)
		{
			sqlSucCount = serviceMapper.insertSelective(service);
		}
		
		return sqlSucCount;
	}

	@Override
	public void registryCreateService(Service service) throws RegistryException {
		RegistryManager.getInstance().setServerService(
				service.getName(),service.getGroup(),service.getHosts());
		
		Set<String> hostSet = new HashSet<String>();
        for(String host : service)
            hostSet.add(host);
        
        for(String host : hostSet) {
        	RegistryManager.getInstance().setServerWeight(host, Constants.WEIGHT_DEFAULT);
        }
		
	}

	@Override
	public String unpublishService(String service, String group, String ip,
									String port, String updatezk) throws RegistryException {
		boolean writeZk = "true".equalsIgnoreCase(updatezk);
	    Service oriService = getService(service, group);
	    Service newService = null;
	    
        if(oriService != null) {
        	newService = new Service(oriService);
        	newService.setHosts(IPUtils.removeHost(oriService.getHosts(), ip, port));
        	updateById(newService);
	        
	        if(writeZk)
	        	registryUpdateService(oriService, newService);
        }
        
        return newService.getHosts();
	}

	@Override
	public JqGridRespBean retrieveByJqGrid(int page, int rows,
			String projectName) {
		JqGridRespBean jqGridTableBean = null;
		
		if(page > 0 && rows > 0 ){
			Project project = projectService.findProject(projectName);
			
			if(project != null){
				Integer projectId = project.getId();
				List<Service> services = serviceMapper.selectByPageRowsProjectId((page - 1) * rows, rows, projectId);
				
				ServiceExample example = new ServiceExample();
				example.createCriteria().andProjectidEqualTo(projectId);
				int totalRecords = serviceMapper.countByExample(example);
				
				int totalPages = (totalRecords - 1) / rows + 1;
				
				jqGridTableBean = new JqGridRespBean();
				jqGridTableBean.setData(services);
				jqGridTableBean.setCurrentPage(page);
				jqGridTableBean.setTotalRecords(totalRecords);
				jqGridTableBean.setTotalPages(totalPages);
			}
			
			
		}
		
		return jqGridTableBean;
	}

}
