/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route;

import java.util.List;

import com.dianping.dpsf.component.DPSFRequest;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.InvokerMetaData;

public interface RouteManager {

	public Client route(List<Client> clientList, InvokerMetaData metaData, DPSFRequest request) throws NetworkException;

}
