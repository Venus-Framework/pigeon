package com.dianping.pigeon.remoting.invoker.exception;

import com.dianping.pigeon.remoting.common.exception.RpcException;

public class RemoteInvocationException extends RpcException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1961488305051802648L;

	public RemoteInvocationException() {
		super();
	}

	public RemoteInvocationException(String message) {
		super(message);
	}

	public RemoteInvocationException(Throwable cause) {
		super(cause);
	}

	public RemoteInvocationException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "0700";
		}
		return errorCode;
	}
}
