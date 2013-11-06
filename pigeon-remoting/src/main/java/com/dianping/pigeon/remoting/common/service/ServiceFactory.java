/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.service;

import java.util.Map;

import com.dianping.dpsf.exception.ServiceException;

/**
 * @author xiangwu
 * @Sep 30, 2013
 * 
 */
public interface ServiceFactory {

	boolean exits(String serviceName);

	Object getService(String serviceName);

	void addService(String serviceName, Object serviceTarget, String ip, int port)
			throws ServiceException;

	void addServices(Map<String, Object> services, String ip, int port)
			throws ServiceException;

	Map<String, Object> getAllServices();

}
