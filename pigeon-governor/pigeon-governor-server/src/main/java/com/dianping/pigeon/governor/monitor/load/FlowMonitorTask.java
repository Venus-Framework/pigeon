package com.dianping.pigeon.governor.monitor.load;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.message.MessageService;
import com.dianping.pigeon.governor.model.ServiceHosts;
import com.dianping.pigeon.governor.monitor.load.impl.DefaultBalanceAnalyzer;
import com.dianping.pigeon.governor.monitor.load.impl.DefaultServerClientComparator;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.dianping.pigeon.governor.util.IPUtils;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by shihuashen on 16/7/14.
 */
public class FlowMonitorTask {
    private Logger logger = LogManager.getLogger(FlowMonitorTask.class.getName());
    private ExecutorService exec;
    private int poolSize = 50;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private MessageService messageService;
    public void start(){
        try {
            messageService.init();
        } catch (Exception e) {
            logger.error(e);
        }
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();
        exec =  Executors.newFixedThreadPool(poolSize);
        List<String> projectNames = projectService.retrieveAllName();
        String time = getPreTime();
        for(Iterator<String> iterator = projectNames.iterator();iterator.hasNext();){
            String projectName = iterator.next();
            exec.submit(new CrossAnalyze(
                    projectName,
                    time,
                    new DefaultServerClientComparator(),
                    new DefaultBalanceAnalyzer(0.5),
                    messageService
            ));
        }
        exec.shutdown();
        try {
            exec.awaitTermination(10, TimeUnit.MINUTES);
            stopwatch.stop();
            GsonUtils.Print(stopwatch.elapsed(TimeUnit.SECONDS)+"s");
            GsonUtils.Print("一次扫描结束");
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    public void schedule(){
        boolean enable = false;
        String server = Lion.get("pigeon-governor-server.flowmonitortask.enable.ip");
        if(org.codehaus.plexus.util.StringUtils.isBlank(server)) {
            logger.warn("服务ip为空");
            return;
        }
        if (IPUtils.getFirstNoLoopbackIP4Address().equals(server)) {
            enable = true;
        }
        if(enable) {
            logger.info("Flow monitor task start");
            Transaction transaction = Cat.newTransaction("FlowMonitorTask", "");
            try {
                start();
                transaction.setStatus(Transaction.SUCCESS);
            }catch(Throwable t){
                logger.error("Purge desc error", t);
                transaction.setStatus(t);
            }finally {
                transaction.complete();
            }
            return ;
        }
    }
    private String getPreTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
        return df.format(calendar.getTime());
    }
}
