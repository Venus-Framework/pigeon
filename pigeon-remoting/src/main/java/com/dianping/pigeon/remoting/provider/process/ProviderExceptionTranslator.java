package com.dianping.pigeon.remoting.provider.process;

import com.dianping.dpsf.exception.DPSFException;
import com.dianping.pigeon.remoting.common.exception.RpcException;

public class ProviderExceptionTranslator {

	public DPSFException translate(Throwable e) {
		if (e == null) {
			return null;
		}
		if (e instanceof RpcException) {
			RpcException rpcEx = (RpcException) e;
			DPSFException newException = new DPSFException(String.format("#%s@%s@%s", rpcEx.getErrorCode(), rpcEx
					.getClass().getSimpleName(), rpcEx.getMessage()));
			newException.setStackTrace(rpcEx.getStackTrace());
			return newException;
		} else {
			DPSFException newException = new DPSFException(String.format("@%s@%s", e.getClass().getSimpleName(),
					e.getMessage()));
			newException.setStackTrace(e.getStackTrace());
			return newException;
		}
	}
}
