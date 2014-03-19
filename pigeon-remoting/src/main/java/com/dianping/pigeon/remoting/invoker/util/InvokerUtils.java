package com.dianping.pigeon.remoting.invoker.util;

import com.dianping.dpsf.exception.NetException;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.Callback;
import com.dianping.pigeon.remoting.invoker.domain.RemoteInvocationBean;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;

public class InvokerUtils {

	private static ServiceInvocationRepository invocationRepository = ServiceInvocationRepository.getInstance();

	public static InvocationResponse sendRequest(Client client, InvocationRequest request, Callback callback) {
		if (request.getCallType() == Constants.CALLTYPE_REPLY) {
			RemoteInvocationBean invocationBean = new RemoteInvocationBean();
			invocationBean.request = request;
			invocationBean.callback = callback;
			callback.setRequest(request);
			callback.setClient(client);
			invocationRepository.put(request.getSequence(), invocationBean);
		}
		InvocationResponse response = null;
		try {
			response = client.write(request, callback);
		} catch (RuntimeException e) {
			invocationRepository.remove(request.getSequence());
			throw new NetException("remote call failed:" + request, e);
		} finally {
			if (response != null) {
				invocationRepository.remove(request.getSequence());
			}
		}
		return response;
	}

}
