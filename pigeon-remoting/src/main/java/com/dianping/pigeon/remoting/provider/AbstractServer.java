package com.dianping.pigeon.remoting.provider;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;
import com.dianping.pigeon.remoting.provider.process.RequestProcessorFactory;

public abstract class AbstractServer implements Server {

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());
	RequestProcessor requestProcessor = null;
	ServerConfig serverConfig = null;

	public abstract void doStart(ServerConfig serverConfig);

	public abstract void doStop();

	public void start(ServerConfig serverConfig) {
		if (logger.isInfoEnabled()) {
			logger.info("server config:" + serverConfig);
		}
		requestProcessor = RequestProcessorFactory.selectProcessor(serverConfig);
		doStart(serverConfig);
		this.serverConfig = serverConfig;
	}

	public void stop() {
		doStop();
		if(requestProcessor != null) {
			requestProcessor.stop();
		}
	}

	@Override
	public ServerConfig getServerConfig() {
		return serverConfig;
	}

	@Override
	public void processRequest(InvocationRequest request, ProviderContext providerContext) {
		requestProcessor.processRequest(request, providerContext);
	}

}
