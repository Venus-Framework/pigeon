package com.dianping.pigeon.governor.task;

import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.util.CmdbUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenchongze on 15/10/28.
 */
@Deprecated
public class UpdateProjectTask implements Runnable {

    private ExecutorService proOwnerThreadPool = new ThreadPoolExecutor(2, 4, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private Logger logger = LogManager.getLogger();

    private ProjectService projectService;

    private ProjectOwnerService projectOwnerService;

    public UpdateProjectTask(ProjectService projectService, ProjectOwnerService projectOwnerService) {
        this.projectService = projectService;
        this.projectOwnerService = projectOwnerService;
    }

    @Override
    public void run() {
        try {
            for(int page = 1;page<Integer.MAX_VALUE; ++page) {
                List<Project> projects = CmdbUtils.getProjectsInfoByPage(page);

                if(projects.size() == 0) {
                    break;
                }

                for(Project project : projects) {
                    try {
                        projectService.create(project);
                        project = projectService.findProject(project.getName());

                        //创建项目默认拥有者
                        if(project != null) {
                            // 加入线程池并发处理创建新项目和新项目管理员
                            final String projectName = project.getName();
                            final String emails = project.getEmail();
                            proOwnerThreadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    //create default project owner
                                    //TODO product from workflow
                                    projectOwnerService.createDefaultOwner(emails, projectName);
                                }
                            });
                        }

                    } catch (DataAccessException e) {
                        logger.error("create project error!", project.getName());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("refresh project error!", e);
        } finally {
        }
    }
}
