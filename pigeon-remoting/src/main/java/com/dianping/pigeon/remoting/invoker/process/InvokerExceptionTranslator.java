package com.dianping.pigeon.remoting.invoker.process;

import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.invoker.exception.RemoteInvocationException;

/**
 * @deprecated
 */
public class InvokerExceptionTranslator {

	private RpcException translate(String errorCode, String msg, StackTraceElement[] stackTrace) {
		RpcException e = new RemoteInvocationException(msg);
		if (e != null) {
			e.setStackTrace(stackTrace);
		}
		return e;
	}
}
