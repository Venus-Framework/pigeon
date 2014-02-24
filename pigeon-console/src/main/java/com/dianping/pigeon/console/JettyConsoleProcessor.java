/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.console;

import java.net.URL;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.console.servlet.json.InvokeJsonServlet;
import com.dianping.pigeon.console.servlet.json.ServiceJsonServlet;
import com.dianping.pigeon.console.servlet.json.ServiceStatusJsonServlet;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.http.provider.JettyHttpServerProcessor;

public class JettyConsoleProcessor implements JettyHttpServerProcessor {

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());

	@Override
	public void preStart(Server server, Context context) {
		int port = server.getConnectors()[0].getPort();
		context.addServlet(new ServletHolder(new ServiceServlet(port)), "/services");
		context.addServlet(new ServletHolder(new ServiceJsonServlet(port)), "/services.json");
		context.addServlet(new ServletHolder(new InvokeJsonServlet(port)), "/invoke.json");
		context.addServlet(new ServletHolder(new ServiceStatusJsonServlet(port)), "/services.status");

		ServletHolder holder = new ServletHolder(new DefaultServlet());
		URL url = JettyConsoleProcessor.class.getClassLoader().getResource("statics");
		if (url == null) {
			logger.error("can't find console static files!");
			return;
		}
		String staticsDir = url.toExternalForm();
		holder.setInitParameter("resourceBase", staticsDir);
		holder.setInitParameter("gzip", "false");
		context.addServlet(holder, "/jquery/*");
		context.addServlet(holder, "/ztree/*");
		context.addServlet(holder, "/bootstrap/*");
	}

}
