/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.event;

/**
 * 
 * @author jianhuihuang
 * 
 */
public interface RuntimeServiceListener {

	boolean support(RuntimeServiceEvent event);

	void handleEvent(RuntimeServiceEvent event);

}
