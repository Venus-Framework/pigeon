package com.dianping.pigeon.remoting.invoker.exception;

import com.dianping.pigeon.remoting.common.exception.RpcException;

public class ServiceUnavailableException extends RpcException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1961488305051802648L;

	public ServiceUnavailableException() {
		super();
	}

	public ServiceUnavailableException(String message) {
		super(message);
	}

	public ServiceUnavailableException(Throwable cause) {
		super(cause);
	}

	public ServiceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "0600";
		}
		return errorCode;
	}
}
