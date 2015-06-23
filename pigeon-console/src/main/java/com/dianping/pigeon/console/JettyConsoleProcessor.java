/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.console;

import java.net.URL;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

import com.dianping.pigeon.console.servlet.ServiceOfflineServlet;
import com.dianping.pigeon.console.servlet.ServiceOnlineServlet;
import com.dianping.pigeon.console.servlet.ServicePublishServlet;
import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.console.servlet.ServiceUnpublishServlet;
import com.dianping.pigeon.console.servlet.json.DependencyJsonServlet;
import com.dianping.pigeon.console.servlet.json.InvokeJsonServlet;
import com.dianping.pigeon.console.servlet.json.ServiceJsonServlet;
import com.dianping.pigeon.console.servlet.json.ServiceStatusJsonServlet;
import com.dianping.pigeon.console.servlet.json.StatisticsJsonServlet;
import com.dianping.pigeon.remoting.http.provider.JettyHttpServerProcessor;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

public class JettyConsoleProcessor implements JettyHttpServerProcessor {

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());

	@Override
	public void preStart(ServerConfig serverConfig, Server server, Context context) {
		int port = server.getConnectors()[0].getPort();
		context.addServlet(new ServletHolder(new ServiceServlet(serverConfig, port)), "/services");
		context.addServlet(new ServletHolder(new ServiceJsonServlet(serverConfig, port)), "/services.json");
		context.addServlet(new ServletHolder(new InvokeJsonServlet(serverConfig, port)), "/invoke.json");
		context.addServlet(new ServletHolder(new ServiceStatusJsonServlet(serverConfig, port)), "/services.status");
		context.addServlet(new ServletHolder(new ServicePublishServlet(serverConfig, port)), "/services.publish");
		context.addServlet(new ServletHolder(new ServiceUnpublishServlet(serverConfig, port)), "/services.unpublish");
		context.addServlet(new ServletHolder(new DependencyJsonServlet(serverConfig, port)), "/dependencies.json");
		context.addServlet(new ServletHolder(new ServiceOnlineServlet(serverConfig, port)), "/services.online");
		context.addServlet(new ServletHolder(new ServiceOfflineServlet(serverConfig, port)), "/services.offline");
		context.addServlet(new ServletHolder(new StatisticsJsonServlet(serverConfig, port)), "/stats.json");

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
