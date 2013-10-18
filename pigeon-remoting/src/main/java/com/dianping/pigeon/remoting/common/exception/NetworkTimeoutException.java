/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.exception;

public class NetworkTimeoutException extends NetworkException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1961488305051802648L;

	public NetworkTimeoutException() {
		super();
	}

	public NetworkTimeoutException(String message) {
		super(message);
	}

	public NetworkTimeoutException(Throwable cause) {
		super(cause);
	}

	public NetworkTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
