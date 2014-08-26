package com.dianping.pigeon.governor.task;

import org.apache.log4j.Logger;

public class HealthCheckBean {

    private static Logger logger = Logger.getLogger(HealthCheckBean.class);
    
    public HealthCheckBean() {
    }
    
    public void init() {
        new HealthCheckManager().start();
        logger.info("HealthCheckManager started");
    }
    
    public static void main(String[] args) throws Exception {
    	HealthCheckBean bean = new HealthCheckBean();
		bean.init();
		System.in.read();
	}
}
