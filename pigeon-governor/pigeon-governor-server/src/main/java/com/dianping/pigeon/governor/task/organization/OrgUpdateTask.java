package com.dianping.pigeon.governor.task.organization;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.dao.CustomOrgMapper;
import com.dianping.pigeon.governor.dao.ProjectMapper;
import com.dianping.pigeon.governor.dao.ProjectOrgMapper;
import com.dianping.pigeon.governor.model.OpLog;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.ProjectOrg;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.OpLogService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.UserService;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.dianping.pigeon.governor.util.IPUtils;
import com.dianping.pigeon.governor.util.OpType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by shihuashen on 16/8/11.
 */
public class OrgUpdateTask {
    private Logger logger = LogManager.getLogger(OrgUpdateTask.class);
    @Autowired
    private ProjectService projectService;
    @Autowired
    private CustomOrgMapper customOrgMapper;
    @Autowired
    private ProjectOrgMapper projectOrgMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private OpLogService opLogService;


    private ExecutorService exec;
    private int poolSize = 20;
    private InfoFetcher infoFetcher = new InfoFetcher();

    public void start(){
        this.exec =  Executors.newFixedThreadPool(poolSize);
        List<String> projectNames = projectService.retrieveAllName();

        for(final Iterator<String> iterator = projectNames.iterator();
            iterator.hasNext();){
            final String projectName = iterator.next();
            exec.submit(new Runnable() {
                @Override
                public void run() {
                    Project project = infoFetcher.getProject(projectName);
                    Project project1 = projectService.findProject(projectName);
                    if(project!=null){
                        if(customOrgMapper.updateProject(project)!=0&&!infoEqual(project1,project)){
                            logOp(project,project1);
                        }
                    }
                    ProjectOrg projectOrg = infoFetcher.getProjectOrg(projectName);
                    if(customOrgMapper.updateProjectOrg(projectOrg)==0){
                        projectOrgMapper.insertSelective(projectOrg);
                    }
                }
            });
        }
        exec.shutdown();
        try {
            exec.awaitTermination(10, TimeUnit.MINUTES);
            System.out.println("Org update Finish");
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    public void schedule(){
        boolean enable = false ;
        String server = Lion.get("pigeon-governor-server.orgupdate.enable.ip");
        if(org.codehaus.plexus.util.StringUtils.isBlank(server)) {
            logger.warn("服务ip为空");
            return;
        }
        if (IPUtils.getFirstNoLoopbackIP4Address().equals(server)) {
            enable = true;
        }
        if(enable) {
            logger.info("Organization update task start");
            Transaction transaction = Cat.newTransaction("OrgUpdateTask", "");
            try {
                start();
                transaction.setStatus(Transaction.SUCCESS);
            }catch(Throwable t){
                logger.error("OrgUpdate task error", t);
                transaction.setStatus(t);
            }finally {
                transaction.complete();
            }
            return ;
        }
    }
    private void logOp(Project project,Project Origin){
        String newProjectInfo = GsonUtils.toJson(project);
        String originProjectInfo = GsonUtils.toJson(Origin);
        String projectName = project.getName();
        int projectId = projectService.findProject(projectName).getId();
        String content = "Update project: "+originProjectInfo+" to "+newProjectInfo;
        OpType opType = OpType.PROJECT_INFO_UPDATE;
        User user = userService.retrieveByDpaccount("pigeon");
        String currentUser = user.getDpaccount();
        String reqIp = IPUtils.getFirstNoLoopbackIP4Address();
        OpLog opLog = new OpLog();
        opLog.setDpaccount(currentUser);
        opLog.setProjectid(projectId);
        opLog.setReqip(reqIp);
        opLog.setOptime(new Date());
        opLog.setContent(content);
        opLog.setOptype(opType.getValue());
        opLogService.create(opLog);
    }
    private boolean infoEqual(Project origin,Project cmdb){
        String bu = cmdb.getBu();
        String email = cmdb.getEmail();
        Integer level = cmdb.getLevel();
        String owner = cmdb.getOwner();
        String phone = cmdb.getPhone();
        if(bu!=null&&!bu.equals(origin.getBu()))
            return false;
        if(email!=null&&!email.equals(origin.getEmail()))
            return false;
        if(level!=null&&!level.equals(origin.getLevel()))
            return false;
        if(owner!=null&&!owner.equals(origin.getOwner()))
            return false;
        if(phone!=null&&!phone.equals(origin.getPhone()))
            return false;
        return true;
    }

}
