/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service;

public class EchoException extends RuntimeException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 0L;

	public EchoException() {
		super();
	}

	public EchoException(String message) {
		super(message);
	}

	public EchoException(Throwable cause) {
		super(cause);
	}

	public EchoException(String message, Throwable cause) {
		super(message, cause);
	}

}
