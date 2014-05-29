/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.exception;

import com.dianping.dpsf.exception.DPSFException;

public class RpcException extends DPSFException {

	private static final long serialVersionUID = -4052834884778586750L;

	private String errorCode;

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

	public RpcException(Throwable cause) {
		super(cause);
	}

	public RpcException(String msg, String errorCode, Throwable cause) {
		super(msg, cause);
		this.errorCode = errorCode;
	}

	public RpcException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
