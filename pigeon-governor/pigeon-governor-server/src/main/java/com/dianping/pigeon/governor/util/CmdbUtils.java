package com.dianping.pigeon.governor.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dianping.pigeon.governor.bean.*;
import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.governor.model.Project;

/**
 * 
 * @author chenchongze
 *
 */
public class CmdbUtils {

	private static final String PROJECTS_INFO_BASE = Constants.CMDB_API_BASE + "/projects?page=";

	private static final String PROJECT_INFO_BASE = Constants.CMDB_API_BASE + "/projects/{project_name}";
	
	private static final String PROJECT_BU_INFO_BASE = Constants.CMDB_API_BASE + "/projects/{project_name}/bu";

	/**
	 *
	 * @param page
	 * @return not null, but .size() == 0
	 */
	public static List<Project> getProjectsInfoByPage(Integer page) {
		List<Project> projects = new ArrayList<Project>();

		String url = PROJECTS_INFO_BASE + page;
		CmdbProjects cmdbProjects = RestCallUtils.getRestCall(url, CmdbProjects.class);

		if(cmdbProjects != null && cmdbProjects.getTotal() > 0) {
			List<CmdbProjectBean> cmdbProjectBeans = cmdbProjects.getProjects();

			for(CmdbProjectBean cmdbProjectBean : cmdbProjectBeans) {
				Project project = new Project();

				if(cmdbProjectBean != null) {
					project.setLevel(cmdbProjectBean.getProject_level());
					project.setOwner(cmdbProjectBean.getRd_duty());
					project.setEmail(cmdbProjectBean.getProject_email());
					project.setPhone(cmdbProjectBean.getRd_mobile());
					//拉bu信息
					url = PROJECT_BU_INFO_BASE.replace("{project_name}", cmdbProjectBean.getProject_name());
					CmdbSingleBu cmdbSingleBu = RestCallUtils.getRestCall(url, CmdbSingleBu.class);

					if(cmdbSingleBu != null) {
						CmdbBuBean cmdbBuBean = cmdbSingleBu.getBu();

						if(cmdbBuBean != null) {
							project.setBu(cmdbBuBean.getBu_name());
						}
					}

					project.setName(cmdbProjectBean.getProject_name());
					Date now = new Date();
					project.setCreatetime(now);
					project.setModifytime(now);

					projects.add(project);
				}
			}
		}

		return projects;
	}
	
	public static Project getProjectInfo(String projectName) {
		Project project = new Project();
		
		//拉项目基本信息
    	String url = PROJECT_INFO_BASE.replace("{project_name}", projectName);
    	CmdbSingleProject cmdbSingleProject = RestCallUtils.getRestCall(url, CmdbSingleProject.class);
    	
    	if(cmdbSingleProject != null) {
    		CmdbProjectBean cmdbProjectBean = cmdbSingleProject.getProject();
    		
    		if(cmdbProjectBean != null) {
        		project.setLevel(cmdbProjectBean.getProject_level());
        		project.setOwner(cmdbProjectBean.getRd_duty());
        		project.setEmail(cmdbProjectBean.getProject_email());
        		project.setPhone(cmdbProjectBean.getRd_mobile());
        		//拉bu信息
        		url = PROJECT_BU_INFO_BASE.replace("{project_name}", projectName);
        		CmdbSingleBu cmdbSingleBu = RestCallUtils.getRestCall(url, CmdbSingleBu.class);
        		
        		if(cmdbSingleBu != null) {
        			CmdbBuBean cmdbBuBean = cmdbSingleBu.getBu();
        			
        			if(cmdbBuBean != null) {
        				project.setBu(cmdbBuBean.getBu_name());
        			}
        		}
    		}
    		
    	}
    	
    	project.setName(projectName);
    	Date now = new Date();
		project.setCreatetime(now);
		project.setModifytime(now);
    	
    	return project;
	}

	public static Project getProjectInfoOrNot(String projectName) {
		//拉项目基本信息
		String url = PROJECT_INFO_BASE.replace("{project_name}", projectName);
		CmdbSingleProject cmdbSingleProject = RestCallUtils.getRestCall(url, CmdbSingleProject.class);

		if(cmdbSingleProject != null) {
			CmdbProjectBean cmdbProjectBean = cmdbSingleProject.getProject();

			if(cmdbProjectBean != null) {
				Project project = new Project();
				project.setName(cmdbProjectBean.getProject_name());
				project.setLevel(cmdbProjectBean.getProject_level());
				project.setOwner(cmdbProjectBean.getRd_duty());
				project.setEmail(cmdbProjectBean.getProject_email());
				project.setPhone(cmdbProjectBean.getRd_mobile());
				//拉bu信息
				url = PROJECT_BU_INFO_BASE.replace("{project_name}", projectName);
				CmdbSingleBu cmdbSingleBu = RestCallUtils.getRestCall(url, CmdbSingleBu.class);

				if(cmdbSingleBu != null) {
					CmdbBuBean cmdbBuBean = cmdbSingleBu.getBu();

					if(cmdbBuBean != null) {
						project.setBu(cmdbBuBean.getBu_name());
					}
				}

				return project;
			}

		}

		return null;
	}
	
	public static String getEmail(String dpAccount) {
		String result = null;
		
		if(StringUtils.isNotBlank(dpAccount)) {
			result = dpAccount + "@" + Constants.DP_EMAIL_BASE;
		}
		
		return result;
	}
	
}
