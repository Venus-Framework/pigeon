/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.exception;

public class PigeonRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 8197156489093927276L;

	@SuppressWarnings("unused")
	private String serviceName;
	@SuppressWarnings("unused")
	private String address;

	public PigeonRuntimeException(String msg) {
		super(msg);
	}

	public PigeonRuntimeException(String serviceName, String address, String msg, Throwable cause) {
		super(msg, cause);
		this.serviceName = serviceName;
		this.address = address;
	}

	public PigeonRuntimeException() {
		super();
	}

	public PigeonRuntimeException(Throwable cause) {
		super(cause);
	}

	public PigeonRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
