package com.dianping.pigeon.governor.dao;

import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.ProjectOrg;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by shihuashen on 16/8/11.
 */
public interface CustomOrgMapper {
    int updateProject(@Param("record") Project record);
    int updateProjectOrg(ProjectOrg projectOrg);
    List<ProjectOrg> getAllProjectOrg();
}
