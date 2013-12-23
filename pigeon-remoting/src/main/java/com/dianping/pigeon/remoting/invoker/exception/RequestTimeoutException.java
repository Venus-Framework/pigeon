package com.dianping.pigeon.remoting.invoker.exception;

public class RequestTimeoutException extends RuntimeException {

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

}
