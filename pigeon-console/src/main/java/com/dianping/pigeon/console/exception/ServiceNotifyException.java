package com.dianping.pigeon.console.exception;

public class ServiceNotifyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1961488305051802648L;

	public ServiceNotifyException() {
		super();
	}

	public ServiceNotifyException(String message) {
		super(message);
	}

	public ServiceNotifyException(Throwable cause) {
		super(cause);
	}

	public ServiceNotifyException(String message, Throwable cause) {
		super(message, cause);
	}

}
