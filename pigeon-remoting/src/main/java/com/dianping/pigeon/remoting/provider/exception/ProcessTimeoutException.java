package com.dianping.pigeon.remoting.provider.exception;

public class ProcessTimeoutException extends RuntimeException {

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

}
