package com.dianping.pigeon.remoting.provider.exception;

import com.dianping.pigeon.remoting.common.exception.RpcException;

public class RequestAbortedException extends RpcException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1961488305051802648L;

	public RequestAbortedException() {
		super();
	}

	public RequestAbortedException(String message) {
		super(message);
	}

	public RequestAbortedException(Throwable cause) {
		super(cause);
	}

	public RequestAbortedException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "0900";
		}
		return errorCode;
	}
}
