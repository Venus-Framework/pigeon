package com.dianping.pigeon.governor.task;

import com.dianping.pigeon.governor.service.HostService;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.threadpool.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chenchongze on 15/10/28.
 */
@Deprecated
public class DailyTaskBean {

    private ExecutorService taskPool;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectOwnerService projectOwnerService;

    @Autowired
    private HostService hostService;

    public void init() {
        taskPool = Executors.newCachedThreadPool(new NamedThreadFactory("Pigeon-Governor-CustomTask"));
        //taskPool.submit(new UpdateProjectTask(projectService, projectOwnerService));
        //taskPool.submit(new ClearInvalidHostsInDB(hostService));
    }

}
