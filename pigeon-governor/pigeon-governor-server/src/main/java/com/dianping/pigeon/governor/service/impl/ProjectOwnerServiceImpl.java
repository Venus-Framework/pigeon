package com.dianping.pigeon.governor.service.impl;

import java.util.Date;
import java.util.List;

import com.dianping.ba.hris.md.api.dto.EmployeeDto;
import com.dianping.ba.hris.md.api.service.EmployeeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.pigeon.governor.dao.ProjectOwnerMapper;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.ProjectOwner;
import com.dianping.pigeon.governor.model.ProjectOwnerExample;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.UserService;
import com.dianping.pigeon.governor.util.CmdbUtils;

@Service
public class ProjectOwnerServiceImpl implements ProjectOwnerService {

	private Logger logger = LogManager.getLogger();

	@Autowired
	private ProjectOwnerMapper projectOwnerMapper;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ProjectService projectService;

	@Autowired
	EmployeeService employeeService;
	
	@Override
	public boolean isProjectOwner(String dpaccount, String projectName) {
		Project project = projectService.findProject(projectName);
		
		return isProjectOwner(dpaccount, project);
	}

	@Override
	public boolean isProjectOwner(String dpaccount, Project project) {
		
		if(project != null) {
			ProjectOwnerExample example = new ProjectOwnerExample();
			example.createCriteria().andProjectidEqualTo(project.getId());
			List<ProjectOwner> projectOwners = projectOwnerMapper.selectByExample(example);
			
			if(projectOwners != null && projectOwners.size() > 0) {
				User user = userService.retrieveByDpaccount(dpaccount);
				
				if(user != null){
					Integer userid = user.getId();
					
					for(ProjectOwner projectOwner : projectOwners) {
						
						if(userid.equals(projectOwner.getUserid())){
							
							return true;
						}
					}
				}
				
			}
			
		}
		
		return false;
	}

	@Override
	public void createDefaultOwner(String email) {
		Project project = projectService.retrieveByEmail(email);
		
		if(project != null) {
			String[] emails = email.split(",");

			for(String mail : emails){
				String dpAccount = mail.split("@")[0];
				User user = userService.retrieveByDpaccount(dpAccount);

				if(user != null) {
					create(user.getId(), project.getId());
				}

				if(user == null) {
					// 从workday-service拉取
					List<EmployeeDto> employeeDtos = employeeService.queryEmployeeByKeyword(dpAccount);
					EmployeeDto employeeDto = null;

					if(employeeDtos != null && employeeDtos.size() == 1) {
						employeeDto = employeeDtos.get(0);

						user = new User();
						user.setDpaccount(dpAccount);
						user.setJobnumber(employeeDto.getEmployeeId());
						user.setUsername(employeeDto.getEmployeeName());
						userService.create(user);

						user = userService.retrieveByDpaccount(dpAccount);

						if(user != null) {
							create(user.getId(),project.getId());
						}

					} else {
						logger.warn("Cannot find user from workday-service");
					}
				}


			}

		}
	}

	@Override
	public void create(Integer userId, Integer projectId) {

		if(userId != null && projectId != null) {
			ProjectOwnerExample projectOwnerExample = new ProjectOwnerExample();
			projectOwnerExample.createCriteria().andUseridEqualTo(userId).andProjectidEqualTo(projectId);
			List<ProjectOwner> projectOwners = projectOwnerMapper.selectByExample(projectOwnerExample);

			if(projectOwners.size() > 0) {
				logger.warn("projectOwner already exists in database");
				return;
			}

			ProjectOwner projectOwner = new ProjectOwner();
			projectOwner.setProjectid(projectId);
			projectOwner.setUserid(userId);
			projectOwner.setCreatetime(new Date());
			projectOwnerMapper.insertSelective(projectOwner);
		}
	}


}
