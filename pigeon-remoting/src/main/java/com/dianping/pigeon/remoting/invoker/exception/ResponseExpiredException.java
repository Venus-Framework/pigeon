package com.dianping.pigeon.remoting.invoker.exception;

public class ResponseExpiredException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1961488305051802648L;

	public ResponseExpiredException() {
		super();
	}

	public ResponseExpiredException(String message) {
		super(message);
	}

	public ResponseExpiredException(Throwable cause) {
		super(cause);
	}

	public ResponseExpiredException(String message, Throwable cause) {
		super(message, cause);
	}

}
