/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.exception;

public class SecurityException extends RpcException {

	private static final long serialVersionUID = -4052834884778586750L;

	public SecurityException() {
		super();
	}

	public SecurityException(String msg) {
		super(msg);
	}

	public SecurityException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "0400";
		}
		return errorCode;
	}
}
