/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.http.provider;

import java.util.List;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.phase.Disposable;
import com.dianping.pigeon.extension.ExtensionLoader;
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
	private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private final int minThreads = configManager.getIntValue("pigeon.provider.http.minthreads", 2);
	private final int maxThreads = configManager.getIntValue("pigeon.provider.http.maxthreads", 300);

	public JettyHttpServer() {
	}

	@Override
	public void destroy() throws Exception {
		this.stop();
	}

	@Override
	public boolean support(ServerConfig serverConfig) {
		if (serverConfig.getProtocol().equals(this.getProtocol())) {
			return true;
		}
		return false;
	}

	private Server newServer(ServerConfig serverConfig) {
		// if (serverConfig.isAutoSelectPort()) {
		int availablePort = NetUtils.getAvailablePort(serverConfig.getHttpPort());
		this.port = availablePort;
		// } else {
		// if (NetUtils.isPortInUse(serverConfig.getHttpPort())) {
		// logger.error("unable to start jetty server on port " +
		// serverConfig.getHttpPort()
		// + ", the port is in use");
		// System.exit(0);
		// }
		// this.port = serverConfig.getHttpPort();
		// }

		DispatcherServlet.addHttpHandler(port, new HttpServerHandler(this));
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setDaemon(true);
		threadPool.setMaxThreads(maxThreads);
		threadPool.setMinThreads(minThreads);
		Server server = new Server(port);
		server.setThreadPool(threadPool);
		Context context = new Context(Context.SESSIONS);
		context.setContextPath("/");
		server.addHandler(context);
		context.addServlet(new ServletHolder(new DispatcherServlet()), "/service");
		List<JettyHttpServerProcessor> processors = ExtensionLoader.getExtensionList(JettyHttpServerProcessor.class);
		if (processors != null) {
			for (JettyHttpServerProcessor processor : processors) {
				processor.preStart(serverConfig, server, context);
			}
		}
		return server;
	}

	@Override
	public void doStart(ServerConfig serverConfig) {
		int retries = 0;
		while (!started) {
			server = newServer(serverConfig);
			retries++;
			try {
				server.start();
				serverConfig.setHttpPort(this.port);
				started = true;
			} catch (Throwable e) {
				if (retries > 3) {
					throw new IllegalStateException("failed to start jetty server on " + serverConfig.getHttpPort()
							+ ", cause: " + e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void doStop() {
		if (server != null) {
			try {
				server.stop();
			} catch (Throwable e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	@Override
	public <T> void doAddService(ProviderConfig<T> providerConfig) {
	}

	@Override
	public <T> void doRemoveService(ProviderConfig<T> providerConfig) {
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

	@Override
	public String getProtocol() {
		return Constants.PROTOCOL_HTTP;
	}
}
