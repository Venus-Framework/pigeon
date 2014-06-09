/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.exception;

public class InvalidParameterException extends RpcException {

	private static final long serialVersionUID = -4052834884778586750L;

	public InvalidParameterException() {
		super();
	}

	public InvalidParameterException(String msg) {
		super(msg);
	}

	public InvalidParameterException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public InvalidParameterException(Throwable cause) {
		super(cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "1000";
		}
		return errorCode;
	}
}
