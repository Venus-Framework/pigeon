package com.dianping.pigeon.governor.bean.scanServiceDesc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.dao.CombinedServiceDescMapper;
import com.dianping.pigeon.governor.model.ServiceHosts;
import com.dianping.pigeon.governor.service.EsService;
import com.dianping.pigeon.governor.service.ServiceDescService;
import com.dianping.pigeon.governor.util.IPUtils;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;

/**
 * Created by shihuashen on 16/6/8.
 */
public class PurgeExpireDescTask {
    @Autowired
    private CombinedServiceDescMapper combinedServiceDescMapper;
    @Autowired
    private EsService esService;
    @Autowired
    private ServiceDescService serviceDescService;

    private Logger logger = LogManager.getLogger(ScanServiceTask.class.getName());
    private ExecutorService exec;
    private int poolSize = 20;
    private String index ;
    private String type;


    public PurgeExpireDescTask(){
        this.index = Lion.get("pigeon-governor-server.es.index","bean");
        this.type = Lion.get("pigeon-governor-server.es.type","camel");
    }



    public void schedule(){
        boolean enable = false;
        String server = Lion.get("pigeon-governor-server.purgeexpiredesctask.enable.ip");
        if(org.codehaus.plexus.util.StringUtils.isBlank(server)) {
            logger.warn("服务ip为空");
            return;
        }
        if (IPUtils.getFirstNoLoopbackIP4Address().equals(server)) {
            enable = true;
        }
        if(enable) {
            logger.info("Purge expire desc task start ...");
            Transaction transaction = Cat.newTransaction("PurgeExpireDescTask", "");
            try {
                startPurge();
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
    public void startPurge(){
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();
        exec =  Executors.newFixedThreadPool(poolSize);
        List<ServiceHosts> serviceHostses = combinedServiceDescMapper.selectServiceHosts();
        final List<Integer> serviceIds = new LinkedList<Integer>();
        Iterator<ServiceHosts> iterator = serviceHostses.iterator();
        while(iterator.hasNext()){
            ServiceHosts serviceHosts = iterator.next();
            serviceIds.add(serviceHosts.getServiceId());
        }
        List<Integer> descIds = combinedServiceDescMapper.selectAllServiceDescBeanId();
        Iterator<Integer> descIdIterator = descIds.iterator();
        while(descIdIterator.hasNext()){
            final int descId = descIdIterator.next();
            exec.submit(new Runnable() {
                @Override
                public void run() {
                    if(!serviceIds.contains(descId)){
                        serviceDescService.removeServiceDesc(descId);
                        esService.remove(index,type,descId);
                    }else{
                        esService.index(index,type,String.valueOf(descId),new Gson().toJson(serviceDescService.getServiceDescBeanById(descId)));
                    }
                }
            });

        }
        exec.shutdown();
        try {
            exec.awaitTermination(30, TimeUnit.MINUTES);
            stopwatch.stop();
            System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));
            logger.info("Purge terminated");
        } catch (InterruptedException e) {
           logger.warn(e);
        }
    }
}
