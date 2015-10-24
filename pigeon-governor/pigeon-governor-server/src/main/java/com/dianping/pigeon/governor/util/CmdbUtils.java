package com.dianping.pigeon.governor.util;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.governor.bean.CmdbBuBean;
import com.dianping.pigeon.governor.bean.CmdbProjectBean;
import com.dianping.pigeon.governor.bean.CmdbSingleBu;
import com.dianping.pigeon.governor.bean.CmdbSingleProject;
import com.dianping.pigeon.governor.model.Project;

/**
 * 
 * @author chenchongze
 *
 */
public class CmdbUtils {

	private static final String PROJECT_INFO_BASE = Constants.CMDB_API_BASE + "/projects/{project_name}";
	
	private static final String PROJECT_BU_INFO_BASE = Constants.CMDB_API_BASE + "/projects/{project_name}/bu";
	
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
	
	public static String getEmail(String dpAccount) {
		String result = null;
		
		if(StringUtils.isNotBlank(dpAccount)) {
			result = dpAccount + "@" + Constants.DP_EMAIL_BASE;
		}
		
		return result;
	}
	
}
