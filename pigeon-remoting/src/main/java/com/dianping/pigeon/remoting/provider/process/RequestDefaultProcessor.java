package com.dianping.pigeon.remoting.provider.process;

import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.actor.RequestActorProcessor;
import com.dianping.pigeon.remoting.provider.process.threadpool.RequestThreadPoolProcessor;

public class RequestDefaultProcessor implements RequestProcessor {

	RequestProcessor akkaProcessor = null;
	RequestProcessor threadProcessor = null;
	ServerConfig serverConfig;

	public RequestDefaultProcessor(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
		akkaProcessor = new RequestActorProcessor(serverConfig);
		threadProcessor = new RequestThreadPoolProcessor(serverConfig);
	}

	public RequestActorProcessor getActorProcessor() {
		return (RequestActorProcessor) akkaProcessor;
	}

	public RequestThreadPoolProcessor getThreadPoolProcessor() {
		return (RequestThreadPoolProcessor) threadProcessor;
	}

	@Override
	public void start() {
		akkaProcessor.start();
		threadProcessor.start();
	}

	@Override
	public void stop() {
		akkaProcessor.stop();
		threadProcessor.stop();
	}

	@Override
	public Future<InvocationResponse> processRequest(InvocationRequest request, ProviderContext providerContext) {
		RequestProcessor processor = threadProcessor;
		if ("actor".equalsIgnoreCase(request.getProcessModel())) {
			processor = akkaProcessor;
		}
		return processor.processRequest(request, providerContext);
	}

	@Override
	public String getProcessorStatistics() {
		StringBuilder str = new StringBuilder();
		str.append("#actor=").append(akkaProcessor.getProcessorStatistics());
		str.append("#threadpool=").append(threadProcessor.getProcessorStatistics());
		return str.toString();
	}

	@Override
	public String getProcessorStatistics(InvocationRequest request) {
		RequestProcessor processor = threadProcessor;
		if ("actor".equalsIgnoreCase(request.getProcessModel())) {
			processor = akkaProcessor;
		}
		return processor.getProcessorStatistics();
	}

	@Override
	public <T> void addService(ProviderConfig<T> providerConfig) {
		akkaProcessor.addService(providerConfig);
		threadProcessor.addService(providerConfig);
	}

	@Override
	public <T> void removeService(ProviderConfig<T> providerConfig) {
		akkaProcessor.removeService(providerConfig);
		threadProcessor.removeService(providerConfig);
	}

	@Override
	public boolean needCancelRequest(InvocationRequest request) {
		RequestProcessor processor = threadProcessor;
		if ("actor".equalsIgnoreCase(request.getProcessModel())) {
			processor = akkaProcessor;
		}
		return processor.needCancelRequest(request);
	}

}
