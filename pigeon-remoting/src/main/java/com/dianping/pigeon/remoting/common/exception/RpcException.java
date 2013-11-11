/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.exception;

public class RpcException extends Exception {

	private static final long serialVersionUID = -4052834884778586750L;

	String errorCode = null;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public RpcException() {
		super();
	}

	public RpcException(String msg) {
		super(msg);
	}

	public RpcException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
