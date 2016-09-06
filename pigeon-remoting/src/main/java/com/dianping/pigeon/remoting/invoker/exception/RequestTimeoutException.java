package com.dianping.pigeon.remoting.invoker.exception;

import com.dianping.dpsf.exception.NetTimeoutException;

public class RequestTimeoutException extends NetTimeoutException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1961488305051802648L;

	public RequestTimeoutException() {
		super();
	}

	public RequestTimeoutException(String message) {
		super(message);
	}

	public RequestTimeoutException(Throwable cause) {
		super(cause);
	}

	public RequestTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "0500";
		}
		return errorCode;
	}
}
