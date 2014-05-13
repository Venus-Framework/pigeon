/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.http.provider;

import java.util.List;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

import com.dianping.pigeon.domain.phase.Disposable;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.http.HttpUtils;
import com.dianping.pigeon.remoting.provider.AbstractServer;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.util.NetUtils;

public class JettyHttpServer extends AbstractServer implements Disposable {

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());
	private Server server;
	private int port;
	private volatile boolean started = false;

	public JettyHttpServer() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public boolean support(ServerConfig serverConfig) {
		if (serverConfig.getProtocols().contains(Constants.PROTOCOL_HTTP)) {
			return true;
		}
		return false;
	}

	@Override
	public void doStart(ServerConfig serverConfig) {
		if (!started) {
			if (serverConfig.isAutoSelectPort()) {
				int availablePort = NetUtils.getAvailablePort(serverConfig.getHttpPort());
				this.port = availablePort;
			} else {
				if (NetUtils.isPortInUse(serverConfig.getHttpPort())) {
					logger.error("unable to start jetty server on port " + serverConfig.getHttpPort()
							+ ", the port is in use");
					System.exit(0);
				}
				this.port = serverConfig.getHttpPort();
			}

			DispatcherServlet.addHttpHandler(port, new HttpServerHandler(this));

			QueuedThreadPool threadPool = new QueuedThreadPool();
			threadPool.setDaemon(true);
			threadPool.setMaxThreads(serverConfig.getMaxPoolSize());
			threadPool.setMinThreads(serverConfig.getCorePoolSize());

			// SelectChannelConnector connector = new SelectChannelConnector();
			// ConfigManager configManager =
			// ExtensionLoader.getExtension(ConfigManager.class);
			// connector.setHost(configManager.getLocalIp());
			// connector.setPort(port);

			server = new Server(port);
			server.setThreadPool(threadPool);
			// server.addConnector(connector);

			Context context = new Context(Context.SESSIONS);
			context.setContextPath("/");
			server.addHandler(context);

			context.addServlet(new ServletHolder(new DispatcherServlet()), "/service");

			// ServletHandler servletHandler = new ServletHandler();
			// ServletHolder servletHolder =
			// servletHandler.addServletWithMapping(DispatcherServlet.class,
			// "/service");
			// servletHolder.setInitOrder(1);
			// server.addHandler(servletHandler);

			List<JettyHttpServerProcessor> processors = ExtensionLoader
					.getExtensionList(JettyHttpServerProcessor.class);
			if (processors != null) {
				for (JettyHttpServerProcessor processor : processors) {
					processor.preStart(serverConfig, server, context);
				}
			}
			try {
				server.start();
				serverConfig.setHttpPort(this.port);
				started = true;
			} catch (Exception e) {
				throw new IllegalStateException("failed to start jetty server on " + serverConfig.getHttpPort()
						+ ", cause: " + e.getMessage(), e);
			}
		}
	}

	@Override
	public void doStop() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	@Override
	public <T> void addService(ProviderConfig<T> providerConfig) throws RpcException {
	}

	@Override
	public String toString() {
		return "JettyServer-" + port;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public String getRegistryUrl(String url) {
		return HttpUtils.getHttpServiceUrl(url);
	}

	@Override
	public List<String> getInvokerMetaInfo() {
		return null;
	}

	@Override
	public boolean isStarted() {
		return started;
	}
}
