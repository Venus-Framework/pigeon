package com.dianping.pigeon.remoting.provider.exception;

import com.dianping.pigeon.remoting.common.exception.RpcException;

public class ProcessTimeoutException extends RpcException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1961488305051802648L;

	public ProcessTimeoutException() {
		super();
	}

	public ProcessTimeoutException(String message) {
		super(message);
	}

	public ProcessTimeoutException(Throwable cause) {
		super(cause);
	}

	public ProcessTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "0900";
		}
		return errorCode;
	}
}
