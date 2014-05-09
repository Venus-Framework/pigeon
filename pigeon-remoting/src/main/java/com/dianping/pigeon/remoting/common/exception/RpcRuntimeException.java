/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.exception;

public class RpcRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -4052834884778586750L;

	String errorCode = null;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public RpcRuntimeException() {
		super();
	}

	public RpcRuntimeException(String msg) {
		super(msg);
	}

	public RpcRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
