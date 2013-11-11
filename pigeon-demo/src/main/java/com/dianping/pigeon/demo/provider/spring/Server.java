/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.spring;

import com.dianping.pigeon.demo.loader.BootstrapLoader;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: Server.java, v 0.1 2013-7-22 上午11:34:45 jianhuihuang Exp $
 */
public class Server {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		BootstrapLoader.startupProvider();
	}

}
