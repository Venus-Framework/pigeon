/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.exception;

public class NetworkException extends RpcException {

	private static final long serialVersionUID = -4052834884778586750L;

	public NetworkException() {
		super();
	}

	public NetworkException(String msg) {
		super(msg);
	}

	public NetworkException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public NetworkException(Throwable cause) {
		super(cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "0200";
		}
		return errorCode;
	}
}
