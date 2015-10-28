package com.dianping.pigeon.governor.task;

import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.threadpool.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chenchongze on 15/10/28.
 */
public class DailyTaskBean {

    private ExecutorService updateProjectPool;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectOwnerService projectOwnerService;

    public void init() {
        updateProjectPool = Executors.newCachedThreadPool(new NamedThreadFactory("Pigeon-Governor-UpdateProject"));
        updateProjectPool.submit(new UpdateProjectTask(projectService, projectOwnerService));
    }

}
