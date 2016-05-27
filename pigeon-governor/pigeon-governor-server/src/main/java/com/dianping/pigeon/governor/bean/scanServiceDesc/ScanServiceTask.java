package com.dianping.pigeon.governor.bean.scanServiceDesc;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.dao.CombinedServiceDescMapper;
import com.dianping.pigeon.governor.model.ServiceHosts;
import com.dianping.pigeon.governor.service.ServiceDescService;
import com.dianping.pigeon.governor.util.IPUtils;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.apache.commons.httpclient.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by shihuashen on 16/4/27.
 */
public class ScanServiceTask {
    private HttpClient httpClient;
    private Gson gson = new Gson();
    private ExecutorService exec;
    @Autowired
    private ServiceDescService serviceDescService;
    @Autowired
    private CombinedServiceDescMapper combinedServiceDescMapper;
    private Logger logger = LogManager.getLogger(ScanServiceTask.class.getName());
    private ConcurrentHashMap<String,UpdateResultState> updateStatics;
    private ConcurrentHashMap<String,JsonHostInfo> cachedInfo;
    private int maxConnectionsPerHost = 20;
    private int maxTotalConnections = 100;
    private int connectionTimeout  =500;
    private int soTimeout = 1000;
    private int poolSize = 50;
    private int timeout = 20;

    //TODO configurable init method
    public ScanServiceTask(){
        MultiThreadedHttpConnectionManager cm = new MultiThreadedHttpConnectionManager();
        cm.getParams().setDefaultMaxConnectionsPerHost(maxConnectionsPerHost);
        cm.getParams().setMaxTotalConnections(maxTotalConnections);
        cm.getParams().setConnectionTimeout(connectionTimeout);
        cm.getParams().setSoTimeout(soTimeout);
        this.httpClient = new HttpClient(cm);
        updateStatics = new ConcurrentHashMap<String, UpdateResultState>();
        exec = Executors.newFixedThreadPool(poolSize);
        cachedInfo =  new ConcurrentHashMap<String, JsonHostInfo>();
    }

    public ScanServiceTask(int maxConnectionsPerHost,int maxTotalConnections,int connectionTimeout,int soTimeout,int poolSize){
        MultiThreadedHttpConnectionManager cm = new MultiThreadedHttpConnectionManager();
        cm.getParams().setDefaultMaxConnectionsPerHost(maxConnectionsPerHost);
        cm.getParams().setMaxTotalConnections(maxTotalConnections);
        cm.getParams().setConnectionTimeout(connectionTimeout);
        cm.getParams().setSoTimeout(soTimeout);
        this.httpClient = new HttpClient(cm);
        updateStatics = new ConcurrentHashMap<String, UpdateResultState>();
        cachedInfo =  new ConcurrentHashMap<String, JsonHostInfo>();
    }
    public void schedule(){
        boolean enable = false;
        String server = Lion.get("pigeon-governor-server.scanservicetask.enable.ip");
        if(org.codehaus.plexus.util.StringUtils.isBlank(server)) {
            logger.warn("服务ip为空");
            return;
        }
        if (IPUtils.getFirstNoLoopbackIP4Address().equals(server)) {
            enable = true;
        }
        if(enable) {
            logger.info("Scan Json Task start ...");
            Transaction transaction = Cat.newTransaction("ScanJsonTask", "");
            try {
                startScanJson();
                transaction.setStatus(Transaction.SUCCESS);
            }catch(Throwable t){
                logger.error("Scan Json error", t);
                transaction.setStatus(t);
            }finally {
                transaction.complete();
            }
            return ;
        }
    }


    public ScanStaticsBean startScanJson(){
        exec = Executors.newFixedThreadPool(poolSize);
        long startTime = System.currentTimeMillis();
        List<ServiceHosts> serviceHostsList = combinedServiceDescMapper.selectServiceHosts();
        Iterator<ServiceHosts> iterator = serviceHostsList.iterator();
        while(iterator.hasNext()){

                final ServiceHosts  serviceHosts = iterator.next();
                UpdateServiceDescTask task = new UpdateServiceDescTask(httpClient,serviceDescService,serviceHosts,gson,updateStatics,cachedInfo);
                exec.submit(task);
        }
        try {
            exec.shutdown();
            exec.awaitTermination(timeout,TimeUnit.MINUTES);
            logger.info("扫描任务执行完毕");

        } catch (InterruptedException e) {
            logger.warn("扫描任务于"+timeout+"分钟内无法完成,任务超时");
            logger.warn(e);
        }
        logger.info("本次更新耗时"+(System.currentTimeMillis()-startTime)/1000/60+"分钟");
        Map<String,UpdateResultState> immutableMap = ImmutableMap.copyOf(updateStatics);
        updateStatics.clear();
        cachedInfo.clear();
        return new ScanStaticsBean(serviceHostsList,immutableMap);
    }
}
