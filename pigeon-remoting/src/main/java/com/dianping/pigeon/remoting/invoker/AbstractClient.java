package com.dianping.pigeon.remoting.invoker;

import java.util.Map;

import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.invoker.callback.Callback;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.listener.HeartBeatListener;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessorFactory;
import com.dianping.pigeon.remoting.invoker.route.region.Region;
import com.dianping.pigeon.remoting.invoker.route.region.RegionPolicyManager;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;

public abstract class AbstractClient implements Client {

	private volatile boolean active = true;

	ResponseProcessor responseProcessor = ResponseProcessorFactory.selectProcessor();

	protected volatile Region region;

	@Override
	public void connectionException(Object attachment, Throwable e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processResponse(InvocationResponse response) {
		this.responseProcessor.processResponse(response, this);
	}

	public InvocationResponse write(InvocationRequest request) throws NetworkException {
		return write(request, null);
	}

	public InvocationResponse write(InvocationRequest request, Callback callback) throws NetworkException {
		ServiceStatisticsHolder.flowIn(request, this.getAddress());
		try {
			return doWrite(request, callback);
		} catch (NetworkException e) {
			ServiceStatisticsHolder.flowOut(request, this.getAddress());
			throw e;
		}
	}

	public abstract InvocationResponse doWrite(InvocationRequest request, Callback callback) throws NetworkException;

	public boolean isActive() {
		return active && HeartBeatListener.isActiveAddress(getAddress());
	}

	public void setActive(boolean active) {
		if (active) {
			ConnectInfo connectInfo = getConnectInfo();
			Map<String, Integer> services = connectInfo.getServiceNames();
			for (String url : services.keySet()) {
				RegistryEventListener.serverInfoChanged(url, connectInfo.getConnect());
			}
		}
		this.active = active;
	}
	@Override
	public Region getRegion() {
		if(region == null) {
			region = RegionPolicyManager.INSTANCE.getRegion(getHost());
		}
		return region;
	}

	@Override
	public void clearRegion() {
		region = null;
	}
}
