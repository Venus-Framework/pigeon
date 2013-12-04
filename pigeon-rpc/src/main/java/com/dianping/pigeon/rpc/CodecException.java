/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.rpc;

public class CodecException extends Exception {

	private static final long serialVersionUID = 4891724546823788256L;

	public CodecException() {
		super();
	}

	public CodecException(String message) {
		super(message);
	}

	public CodecException(Throwable cause) {
		super(cause);
	}

	public CodecException(String message, Throwable cause) {
		super(message, cause);
	}
}
