/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.component;

import java.io.Serializable;

public class RemoteServiceError implements Serializable {

	private String exceptionName;

	private String errorMessage;

	private String errorStackTrace;

	public RemoteServiceError() {
	}

	public RemoteServiceError(String exceptionName, String errorMessage, String errorStackTrace) {
		this.exceptionName = exceptionName;
		this.errorMessage = errorMessage;
		this.errorStackTrace = errorStackTrace;
	}

	public String getExceptionName() {
		return exceptionName;
	}

	public void setExceptionName(String exceptionName) {
		this.exceptionName = exceptionName;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorStackTrace() {
		return errorStackTrace;
	}

	public void setErrorStackTrace(String errorStackTrace) {
		this.errorStackTrace = errorStackTrace;
	}

}
