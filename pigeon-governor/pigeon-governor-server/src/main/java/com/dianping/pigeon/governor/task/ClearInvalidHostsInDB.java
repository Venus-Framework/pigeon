package com.dianping.pigeon.governor.task;

import com.dianping.pigeon.governor.model.Host;
import com.dianping.pigeon.governor.service.HostService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenchongze on 15/11/6.
 */
public class ClearInvalidHostsInDB implements Runnable {

    private ExecutorService proOwnerThreadPool = new ThreadPoolExecutor(30, 60, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private HostService hostService;

    public ClearInvalidHostsInDB(HostService hostService) {
        this.hostService = hostService;
    }


    @Override
    public void run() {
        List<Host> hosts = hostService.retrieveAll();

        for(final Host host : hosts) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    int length = host.getIpport().split(":").length;

                    if(length > 1 && length < 10) {
                        //
                    } else {
                        //直接删除数据库记录
                        hostService.deleteById(host.getId());
                    }
                }
            };

            proOwnerThreadPool.submit(run);
        }
    }
}
