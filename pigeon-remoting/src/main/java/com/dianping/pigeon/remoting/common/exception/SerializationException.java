/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.exception;

public class SerializationException extends RpcException {

	private static final long serialVersionUID = -4052834884778586750L;

	public SerializationException() {
		super();
	}

	public SerializationException(String msg) {
		super(msg);
	}

	public SerializationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public SerializationException(Throwable cause) {
		super(cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "0100";
		}
		return errorCode;
	}
}
