package com.dianping.pigeon.governor.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.pigeon.governor.bean.JqGridRespBean;
import com.dianping.pigeon.governor.bean.ProjectBean;
import com.dianping.pigeon.governor.dao.ProjectMapper;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.service.ProjectService;

@Service
public class ProjectServiceImpl implements ProjectService {
	
	@Autowired
	private ProjectMapper projectMapper;
	
	@Override
	public int create(ProjectBean projectBean) {
		int sqlSucCount = -1;
		Project project = projectBean.createProject();
		
		if(StringUtils.isNotBlank(project.getName()) 
						&& project.getCreatetime() != null
						&& project.getModifytime() != null)
		{
			sqlSucCount = projectMapper.insertSelective(project);
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
				count = projectMapper.deleteByPrimaryKey(id);;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}finally{
				sqlSucCount += count;
			}
			
		}
		
		return sqlSucCount;
	}


	@Override
	public int updateById(ProjectBean projectBean) {
		int sqlSucCount = -1;
		Project project = projectBean.convertToProject();
		
		if(StringUtils.isNotBlank(project.getName())
						&& project.getCreatetime() != null
						&& project.getModifytime() != null
						&& project.getId() != null
						&& project.getId() > 0)
		{
			sqlSucCount = projectMapper.updateByPrimaryKey(project);
		}
		
		return sqlSucCount;
	}

	@Override
	public JqGridRespBean retrieveByJqGrid(int page, int rows) {
		JqGridRespBean jqGridTableBean = null;
		
		if(page > 0){
			List<Project> projects = projectMapper.selectByPageAndRows((page - 1) * rows, rows);
			int totalRecords = projectMapper.countByExample(null);
			int totalPages = (totalRecords - 1) / rows + 1;
			
			jqGridTableBean = new JqGridRespBean();
			jqGridTableBean.setData(projects);
			jqGridTableBean.setCurrentPage(page);
			jqGridTableBean.setTotalRecords(totalRecords);
			jqGridTableBean.setTotalPages(totalPages);
		}
		
		return jqGridTableBean;
	}

}
