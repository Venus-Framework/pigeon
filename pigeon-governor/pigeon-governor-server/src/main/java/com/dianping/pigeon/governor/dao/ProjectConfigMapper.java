package com.dianping.pigeon.governor.dao;

import java.util.List;

/**
 * Created by shihuashen on 16/5/24.
 */
public interface ProjectConfigMapper {
    List<String> getServiceNames(String projectName);
    Integer getProjectCount(String projectName);
}
