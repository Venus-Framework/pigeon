package com.dianping.pigeon.governor.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.dianping.pigeon.governor.util.ThreadPoolFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.dianping.pigeon.governor.bean.JqGridRespBean;
import com.dianping.pigeon.governor.bean.ServiceBean;
import com.dianping.pigeon.governor.dao.ServiceMapper;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.model.ServiceExample;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.RegistryService;
import com.dianping.pigeon.governor.service.ServiceService;
import com.dianping.pigeon.governor.util.IPUtils;
import org.springframework.dao.DataAccessException;

/**
 * 
 * @author chenchongze
 *
 */
@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {

	private Logger logger = LogManager.getLogger();

	private ExecutorService proOwnerThreadPool = ThreadPoolFactory.getWorkThreadPool();
	
	@Autowired
	private ServiceMapper serviceMapper;
	@Autowired
	private ProjectService projectService;
	@Autowired
	@Qualifier("registrySerivce")
	private RegistryService registryService;
	@Autowired
	private ProjectOwnerService projectOwnerService;

	private List<Service> servicesCache;
	private long servicesCacheLastUpdateTime = 0;
	private long checkCacheInternal = 20000;
	
	@Override
	public int create(ServiceBean serviceBean, String updatezk) throws Exception {
		int count = 0;
		boolean writeZk = "true".equalsIgnoreCase(updatezk);
		Service service = serviceBean.createService();
		count = create(service);
		
		if(count > 0 && writeZk) {
			try {
				registryService.registryCreateService(service);
			} catch (Exception e) {
				logger.error("update zk error", e);
				service = getService(service.getName(), service.getGroup());
				if(service != null) {
					deleteById(service.getId());
				}
				throw e;
			}
		}
		
		return count;
	}
	
	@Override
	public int deleteById(Integer id) {
		int sqlSucCount = 0;
		
		if(id != null){
			sqlSucCount = serviceMapper.deleteByPrimaryKey(id);
		}
		
		return sqlSucCount;
	}
	
	@Override
	public int deleteByIdSplitByComma(String idsComma, String updatezk) throws Exception {
		boolean writeZk = "true".equalsIgnoreCase(updatezk);
		int sqlSucCount = 0;
		String idsArr[] = idsComma.split(",");
		
		for(String ids : idsArr){
			int id;
			int count = 0;
			
			try {
				id = Integer.parseInt(ids);
				Service service = serviceMapper.selectByPrimaryKey(id);
				
				if(service != null){
					count = serviceMapper.deleteByPrimaryKey(id);
					
					if(count > 0 && writeZk){
						registryService.registryDeleteService(service);
						
						if(StringUtils.isNotEmpty(service.getGroup())) {
							ServiceExample example = new ServiceExample();
							example.createCriteria().andNameEqualTo(service.getName());
				            List<Service> services = serviceMapper.selectByExample(example);
				            
				            if(services != null && services.size() == 0) {
				                // Last service for name, delete parent node in ZK
				                service.setGroup("");
				                registryService.registryDeleteService(service);
				            }
				        }
					}
					
				}
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}finally{
				sqlSucCount += count;
			}
			
		}
		
		return sqlSucCount;
	}

	@Override
	public int updateById(ServiceBean serviceBean, String updatezk) throws Exception {
		int count = -1;
		boolean writeZk = "true".equalsIgnoreCase(updatezk);
		Service service = serviceBean.convertToService();
		Service oriService = getService(service.getName(), service.getGroup());
		count = updateById(service);
		
		if(count > 0 && writeZk) {
			registryService.registryUpdateService(oriService, service);
		}
		
		return count;
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
		
		if(page > 0 && rows > 0){
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
		List<Service> services = null;
		try {
			services = serviceMapper.selectByExample(example);
		} catch (DataAccessException e) {
			logger.error(e.getMessage());
		}

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
			try {
				sqlSucCount = serviceMapper.updateByPrimaryKeySelective(service);
			} catch (DataAccessException e) {
				logger.error(e.getMessage());
			}
		}
		
		return sqlSucCount;
	}

	@Override
	public String publishService(final String project, String service, String group,
									String ip, String port, String updatezk) throws Exception {
		boolean writeZk = "true".equalsIgnoreCase(updatezk);
	    Service oriService = getService(service, group);
	    Service newService = null;
	    
	    if(oriService != null) {
	    	newService = new Service(oriService);
	    	newService.setHosts(IPUtils.addHost(newService.getHosts(), ip, port));
	        updateById(newService);
	        
	        if(writeZk)
	        	registryService.registryUpdateService(oriService, newService);
	        
	    } else {

			Project newProject = projectService.findProject(project);

			if(newProject == null){
				newProject = projectService.createProject(project, true);
			}

			if(newProject == null){
				return null;
			}

			// 加入线程池并发处理创建新项目和新项目管理员
			final String emails = newProject.getEmail();
			proOwnerThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					//create default project owner
					//TODO product from workflow
					projectOwnerService.createDefaultOwner(emails, project);
				}
			});


	        newService = new Service();
	        newService.setProjectid(newProject.getId());
	        newService.setName(service);
	        newService.setGroup(group);
	        newService.setHosts(IPUtils.addHost(null, ip, port));
	        create(newService);
	        
	        if(writeZk)
	        	registryService.registryCreateService(newService);
	        
	    }

		if(newService != null){
			return newService.getHosts();
		}

		return null;
	}

	@Override
	public int create(Service service) {
		int sqlSucCount = 0;
		
		if(StringUtils.isNotBlank(service.getName()) 
						&& service.getProjectid() != null)
		{
			try {
				sqlSucCount = serviceMapper.insertSelective(service);
			} catch (DataAccessException e) {
				logger.error(e.getMessage());
			}
		}
		
		return sqlSucCount;
	}


	@Override
	public String unpublishService(String service, String group, String ip,
									String port, String updatezk) throws Exception {
		boolean writeZk = "true".equalsIgnoreCase(updatezk);
	    Service oriService = getService(service, group);
	    Service newService = null;
	    
        if(oriService != null) {
        	newService = new Service(oriService);
        	newService.setHosts(IPUtils.removeHost(oriService.getHosts(), ip, port));
        	updateById(newService);
	        
	        if(writeZk)
	        	registryService.registryUpdateService(oriService, newService);
        }

		if(newService != null){
			return newService.getHosts();
		}
        
        return null;
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

	@Override
	public JqGridRespBean retrieveByJqGrid(String projectName) {
		JqGridRespBean jqGridTableBean = null;

		if(StringUtils.isNotBlank(projectName)) {
			Project project = projectService.findProject(projectName);

			if(project != null){
				Integer projectId = project.getId();
				ServiceExample serviceExample = new ServiceExample();
				serviceExample.createCriteria().andProjectidEqualTo(projectId);
				List<Service> services = serviceMapper.selectByExample(serviceExample);

				jqGridTableBean = new JqGridRespBean();
				jqGridTableBean.setData(services);
				//jqGridTableBean.setCurrentPage(page);
				jqGridTableBean.setTotalRecords(services.size());
				//jqGridTableBean.setTotalPages(totalPages);
			}

		}

		return jqGridTableBean;
	}

	@Override
	public List<Service> retrieveAllByProjectName(String projectName) {
		List<Service> services = new ArrayList<Service>();

		if(StringUtils.isNotBlank(projectName)) {
			Project project = projectService.findProject(projectName);

			if(project != null){
				Integer projectId = project.getId();
				ServiceExample serviceExample = new ServiceExample();
				serviceExample.createCriteria().andProjectidEqualTo(projectId);
				services = serviceMapper.selectByExample(serviceExample);
			}

		}

		return services;
	}

	@Override
	public List<Service> getServiceList(int projectId, String group) {
		ServiceExample example = new ServiceExample();
		example.createCriteria().andProjectidEqualTo(projectId).andGroupEqualTo(group);
		List<Service> services = serviceMapper.selectByExample(example);

		return services;
	}

	@Override
	public int create(Service service, String updatezk) throws Exception {
		int count = 0;
		boolean writeZk = "true".equalsIgnoreCase(updatezk);
		count = create(service);

		if(count > 0 && writeZk) {
			try {
				registryService.registryCreateService(service);
			} catch (Exception e) {
				logger.error("update zk error", e);
				service = getService(service.getName(), service.getGroup());
				if(service != null) {
					deleteById(service.getId());
				}
				throw e;
			}
		}

		return count;
	}

	@Override
	public int updateById(Service service, String updatezk) throws Exception {
		int count = -1;
		boolean writeZk = "true".equalsIgnoreCase(updatezk);
		Service oriService = getService(service.getName(), service.getGroup());
		count = updateById(service);

		if(count > 0 && writeZk) {
			registryService.registryUpdateService(oriService, service);
		}

		return count;
	}

	@Override
	public List<Service> retrieveAllIdNamesByCache() {
		long currentTime = System.currentTimeMillis();
		if(currentTime - servicesCacheLastUpdateTime > checkCacheInternal){
			servicesCache = retrieveAllIdNames();
			servicesCacheLastUpdateTime = currentTime;
		}

		return servicesCache;
	}

	@Override
	public List<Service> retrieveAllIdNames() {
		List<Service> services = null;
		try {
			services = serviceMapper.selectAllServiceIdNames();
		} catch (DataAccessException e) {
			logger.error("DB error",e);
		}
		return services;
	}

	@Override
	public boolean deleteByGroup(int projectId, String group, boolean updateZk) {
		ServiceExample example = new ServiceExample();
		example.createCriteria().andProjectidEqualTo(projectId).andGroupEqualTo(group);
		int resultCount;

		try {
			List<Service> services = serviceMapper.selectByExample(example);
			if(updateZk) {
				for(Service service : services) {
					registryService.registryDeleteService(service);
				}
			}

			resultCount = serviceMapper.deleteByExample(example);
			if(resultCount > 0) {
				return true;
			}
		} catch (DataAccessException e) {
			logger.error("Delete failed!", e);
			return false;
		} catch (Exception e) {
			logger.error("Delete failed!", e);
			return false;
		}

		return false;
	}

}
