/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.exception;

/**
 * @author danson.liu
 * 
 */
public class RegistryException extends Exception {

	private static final long serialVersionUID = -277294587317829825L;

	public RegistryException(String msg) {
		super(msg);
	}

	public RegistryException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public RegistryException(Throwable cause) {
		super(cause);
	}

}
