/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker;

import com.dianping.pigeon.remoting.invoker.component.ConnectInfo;

/**
 * 
 * @author xiangwu
 * @Sep 11, 2013
 * 
 */
public interface ClientFactory {

	Client createClient(ConnectInfo cmd);
}
