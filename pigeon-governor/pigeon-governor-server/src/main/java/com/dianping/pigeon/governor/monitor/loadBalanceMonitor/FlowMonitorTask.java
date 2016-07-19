package com.dianping.pigeon.governor.monitor.loadBalanceMonitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;

/**
 * Created by shihuashen on 16/7/14.
 */
public class FlowMonitorTask {
    //TODO fix logger
    private Logger logger = LogManager.getLogger(FlowMonitorTask.class.getName());
    private ExecutorService exec;
    private int poolSize = 50;
    
}
