package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.serviceTree.TreeNode;
import com.dianping.pigeon.governor.model.ProjectOrg;

import java.util.List;
import java.util.Set;

/**
 * Created by shihuashen on 16/8/12.
 */
public interface ServiceTreeService {
    TreeNode getFullTree();
    Set<String> getMyProject(String dpAccount);
    ProjectOrg getProjectOrg(String projectName);
}
