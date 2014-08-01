/**
 * 
 */
package com.dianping.pigeon.log;

import org.apache.log4j.Logger;

/**
 * <p>
 * Title: pigeonLog.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author saber miao
 * @version 1.0
 * @created 2010-9-2 下午05:58:39
 */
public class LoggerLoader {
    
	private LoggerLoader() {
	}

	private static volatile boolean customLog4j = false;
	
	static {
	    try {
            Class.forName("org.apache.log4j.Hierarchy");
            customLog4j = true;
        } catch (ClassNotFoundException e) {
            customLog4j = false;
        }
	}
	
	public static Logger getLogger(Class clazz) {
		return getLogger(clazz.getName());
	}

	public static Logger getLogger(String name) {
	    Logger logger = null;
	    if(customLog4j) {
	        logger = CustomLog4jFactory.getLogger(name);
	    }
	    else {
	        logger = Logger.getLogger(name);
	    }
		return logger;
	}

}
