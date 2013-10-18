/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.exception;

import com.dianping.pigeon.exception.PigeonRuntimeException;

public class NetworkException extends PigeonRuntimeException {

	private static final long serialVersionUID = -5839497325867298648L;

	public NetworkException() {
		super();
	}

	public NetworkException(String message) {
		super(message);
	}

	public NetworkException(Throwable cause) {
		super(cause);
	}

	public NetworkException(String message, Throwable cause) {
		super(message, cause);
	}

}
