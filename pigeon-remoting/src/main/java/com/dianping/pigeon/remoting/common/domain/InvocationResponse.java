/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.domain;

public interface InvocationResponse extends InvocationSerializable {

	void setMessageType(int messageType);

	int getMessageType();

	String getCause();

	Object getReturn();

	void setReturn(Object obj);

}
