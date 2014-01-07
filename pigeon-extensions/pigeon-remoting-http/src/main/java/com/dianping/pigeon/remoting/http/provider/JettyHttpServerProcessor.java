/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.http.provider;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;

public interface JettyHttpServerProcessor {

	public void preStart(Server server, Context context);
}
