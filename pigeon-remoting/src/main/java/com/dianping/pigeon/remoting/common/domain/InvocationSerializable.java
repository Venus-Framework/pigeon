/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.domain;

import java.io.Serializable;

public interface InvocationSerializable extends Serializable {

	int getMessageType();

	void setMessageType(int messageType);

	byte getSerialize();

	void setSerialize(byte serialize);

	void setSequence(long seq);

	long getSequence();

	Object getObject();

	Object getContext();

	void setContext(Object context);

	void setSize(int size);

	int getSize();
}
