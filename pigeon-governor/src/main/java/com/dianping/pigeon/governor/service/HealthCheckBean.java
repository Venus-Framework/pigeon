package com.dianping.pigeon.governor.service;

import org.apache.log4j.Logger;

import com.dianping.pigeon.governor.task.HealthCheckManager;

public class HealthCheckBean {

    private static Logger logger = Logger.getLogger(HealthCheckBean.class);
    
    public HealthCheckBean() {
    }
    
    public void init() {
        new HealthCheckManager().start();
        logger.info("HealthCheckManager started");
    }
}
