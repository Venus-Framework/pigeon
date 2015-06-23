package com.dianping.pigeon.remoting.common.exception;


public class ServiceStatusException extends RpcException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1961488305051802648L;

	public ServiceStatusException() {
		super();
	}

	public ServiceStatusException(String message) {
		super(message);
	}

	public ServiceStatusException(Throwable cause) {
		super(cause);
	}

	public ServiceStatusException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "0600";
		}
		return errorCode;
	}
}
