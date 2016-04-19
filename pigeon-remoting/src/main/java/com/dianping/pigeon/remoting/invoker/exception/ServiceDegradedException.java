/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.exception;

import com.dianping.pigeon.remoting.common.exception.RpcException;

public class ServiceDegradedException extends RpcException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1701044801180185353L;


	public ServiceDegradedException() {
		super();
	}

	public ServiceDegradedException(String message) {
		super(message);
	}

	public ServiceDegradedException(Throwable cause) {
		super(cause);
	}

	public ServiceDegradedException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getErrorCode() {
		if (errorCode == null) {
			return "1100";
		}
		return errorCode;
	}
}
