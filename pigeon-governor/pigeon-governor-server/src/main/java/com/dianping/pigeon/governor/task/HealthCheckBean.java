package com.dianping.pigeon.governor.task;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;


public class HealthCheckBean {

    private static Logger logger = LoggerLoader.getLogger(HealthCheckBean.class);
    
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
