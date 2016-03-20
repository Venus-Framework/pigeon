package com.dianping.pigeon.remoting.provider.process;

import com.dianping.pigeon.remoting.common.exception.RpcException;

public class ProviderExceptionTranslator {

	public RpcException translate(Throwable e) {
		if (e == null) {
			return null;
		}
		if (e instanceof RpcException) {
			return (RpcException) e;
		} else {
			RpcException newException = new RpcException(String.format("@%s@%s", e.getClass().getSimpleName(),
					e.getMessage()));
			newException.setStackTrace(e.getStackTrace());
			return newException;
		}
	}
}
