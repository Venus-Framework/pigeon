package com.dianping.pigeon.governor.service.impl;

import com.dianping.pigeon.governor.bean.serviceTree.TreeNode;
import com.dianping.pigeon.governor.dao.CustomOrgMapper;
import com.dianping.pigeon.governor.dao.ProjectMapper;
import com.dianping.pigeon.governor.dao.ProjectOrgMapper;
import com.dianping.pigeon.governor.dao.ProjectOwnerMapper;
import com.dianping.pigeon.governor.model.*;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.ServiceTreeService;
import com.dianping.pigeon.governor.service.UserService;
import com.dianping.pigeon.governor.util.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by shihuashen on 16/8/12.
 */
@Service
public class ServiceTreeServiceImpl implements ServiceTreeService{
    @Autowired
    private CustomOrgMapper customOrgMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectOwnerMapper projectOwnerMapper;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectOrgMapper projectOrgMapper;
    @Override
    public TreeNode getFullTree() {
        Map<String,Map<String,List<String>>> map = constructMap();
        List<TreeNode> list  = new LinkedList<TreeNode>();
        for(Iterator<String> buIter =  map.keySet().iterator();
                buIter.hasNext();){
            String buName = buIter.next();
            List<TreeNode> buNode = new LinkedList<TreeNode>();
            for(Iterator<String> productIter = map.get(buName).keySet().iterator();
                    productIter.hasNext();){
                List<TreeNode> productNode = new LinkedList<TreeNode>();
                String productName = productIter.next();
                for(Iterator<String> projectIter = map.get(buName).get(productName).iterator();
                        projectIter.hasNext();){
                    String projectName = projectIter.next();
                    productNode.add(new TreeNode(projectName,new LinkedList<TreeNode>()));
                }
                buNode.add(new TreeNode(productName,productNode));
            }
            list.add(new TreeNode(buName,buNode));
        }
        TreeNode root = new TreeNode("root",list);
        return root;
    }

    @Override
    public Set<String> getMyProject(String dpAccount) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOwnerEqualTo(dpAccount);
        List<Project> projects = new LinkedList<Project>();
        try {
            projects= projectMapper.selectByExample(projectExample);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        User user = userService.retrieveByDpaccount(dpAccount);
        ProjectOwnerExample projectOwnerExample = new ProjectOwnerExample();
        projectOwnerExample.createCriteria().andUseridEqualTo(user.getId());
        List<ProjectOwner> projectOwners = new LinkedList<ProjectOwner>();
        try {
            projectOwners = projectOwnerMapper.selectByExample(projectOwnerExample);
        } catch (DataAccessException e){
            e.printStackTrace();
        }
        HashSet<String> set = new HashSet<String>();
        for(ProjectOwner projectOwner : projectOwners ){
            Project project = projectService.retrieveProjectById(projectOwner.getProjectid());
            set.add(project.getName());
        }
        for(Project project : projects)
            set.add(project.getName());
        return set;
    }

    @Override
    public ProjectOrg getProjectOrg(String projectName) {
        ProjectOrgExample projectOrgExample = new ProjectOrgExample();
        projectOrgExample.createCriteria().andNameEqualTo(projectName);
        List<ProjectOrg> projectOrgs = new LinkedList<ProjectOrg>();
        try {
            projectOrgs = projectOrgMapper.selectByExample(projectOrgExample);
        }catch (DataAccessException e){
            e.printStackTrace();
        }
        if(projectOrgs.size()==1)
            return projectOrgs.get(0);
        else
            return new ProjectOrg();
    }


    private Map<String,Map<String,List<String>>> constructMap(){
        Map<String,Map<String,List<String>>> map = new HashMap<String, Map<String, List<String>>>();
        List<ProjectOrg> projectOrgs =  customOrgMapper.getAllProjectOrg();
        for(Iterator<ProjectOrg> iterator = projectOrgs.iterator();
                iterator.hasNext();){
            ProjectOrg projectOrg = iterator.next();
            String buName = projectOrg.getBu();
            String projectName = projectOrg.getName();
            String productName = projectOrg.getProduct();
            if(buName==null)
                buName = "Undefined";
            if(productName==null)
                productName="Undefined";
            if(map.containsKey(buName)){
                Map<String,List<String>> innerMap =  map.get(buName);
                if(innerMap.containsKey(productName)){
                    innerMap.get(productName).add(projectName);
                }else{
                    List<String> list = new LinkedList<String>();
                    list.add(projectName);
                    innerMap.put(productName,list);
                }
            }else{
                Map<String,List<String>> innerMap = new HashMap<String, List<String>>();
                List<String> list = new LinkedList<String>();
                list.add(projectName);
                innerMap.put(productName,list);
                map.put(buName,innerMap);
            }
        }
        return map;
    }

}
