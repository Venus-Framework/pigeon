package com.dianping.pigeon.remoting.provider.exception;

import com.dianping.pigeon.remoting.common.exception.RpcException;

public class InvocationFailureException extends RpcException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1961488305051802648L;

	public InvocationFailureException() {
		super();
	}

	public InvocationFailureException(String message) {
		super(message);
	}

	public InvocationFailureException(Throwable cause) {
		super(cause);
	}

	public InvocationFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "0800";
		}
		return errorCode;
	}
}
