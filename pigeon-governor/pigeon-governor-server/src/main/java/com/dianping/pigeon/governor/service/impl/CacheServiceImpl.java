package com.dianping.pigeon.governor.service.impl;

import com.dianping.pigeon.governor.dao.CustomCacheMapper;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.CacheService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by shihuashen on 16/8/9.
 */
@Service
public class CacheServiceImpl implements CacheService{
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    @Autowired
    private CustomCacheMapper customCacheMapper;

    private Logger logger = LogManager.getLogger(CacheServiceImpl.class.getName());

    private volatile Map<Integer,String> projectNameMap;
    private volatile Map<String,String> userNameMap;
    private volatile List<Project> projects;
    private volatile List<User> users;
    private Thread cacheRefreshThread;
    private boolean isStopped;
    private long interval;
    @PostConstruct
    private void init() throws Exception {
        this.projectNameMap = new HashMap<Integer, String>();
        this.userNameMap = new HashMap<String,String>();
        this.projects = new LinkedList<Project>();
        this.users = new LinkedList<User>();
        this.cacheRefreshThread = new Thread();
        this.isStopped = false;
//        refresh per 5min
        this.interval = 300000;
        start();
    }
    private void start() throws Exception{
        this.cacheRefreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                cacheRefresh();
            }
        });
        this.cacheRefreshThread.setDaemon(true);
        this.cacheRefreshThread.start();
    }
    private void cacheRefresh(){
        while(!checkStop()){
            refreshProjectsInfo();
            refreshUsersInfo();
            try {
                Thread.currentThread().sleep(300000);
            } catch (InterruptedException e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
    }
    private boolean checkStop() {
        return isStopped || Thread.currentThread().isInterrupted();
    }

    private void refreshProjectsInfo(){
        this.projects = projectService.retrieveAllIdNames();
        Map<Integer,String> map = new HashMap<Integer, String>();
        for(Iterator<Project> iterator = projects.iterator();
                iterator.hasNext();){
            Project project = iterator.next();
            map.put(project.getId(),project.getName());
        }
        this.projectNameMap = map;
    }

    private void refreshUsersInfo(){
        this.users = customCacheMapper.getAllUser();
        Map<String,String> map = new HashMap<String, String>();
        for(Iterator<User> iterator = users.iterator();
                iterator.hasNext();){
            User user = iterator.next();
            map.put(user.getDpaccount(),user.getUsername());
        }
        this.userNameMap = map;
    }


    @Override
    public String getProjectNameWithId(int projectId) {
        if(projectNameMap.containsKey(projectId))
            return projectNameMap.get(projectId);
        Project project = projectService.retrieveProjectById(projectId);
        if(project!=null)
            return project.getName();
        else
            return null;
    }

    @Override
    public String getUserNameWithDpAccount(String dpAccount) {
        if(userNameMap.containsKey(dpAccount))
            return userNameMap.get(dpAccount);
        User user = userService.retrieveByDpaccount(dpAccount);
        if(user!=null)
            return user.getUsername();
        else
            return null;
    }
}
