/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.service;

import java.util.Map;

/**
 * @author xiangwu
 * @Sep 30, 2013
 * 
 */
public interface ServiceFactory {

	boolean exits(String serviceName);

	Object getService(String serviceName);

	void addService(String serviceName, Object serviceTarget, int port);

	void addServices(Map<String, Object> services, int port);

	Map<String, Object> getAllServices();

}
