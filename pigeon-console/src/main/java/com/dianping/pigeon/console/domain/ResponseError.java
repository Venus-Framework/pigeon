/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.console.domain;

import org.apache.commons.lang.exception.ExceptionUtils;

public class ResponseError {

	private transient int status;
	private String msg;
	private String cause;

	public ResponseError(String msg, Throwable cause, int status) {
		this.msg = msg;
		this.cause = ExceptionUtils.getFullStackTrace(cause);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

}
