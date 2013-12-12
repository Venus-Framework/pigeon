/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.domain;

public interface InvocationRequest extends InvocationSerializable {

	void setCallType(int callType);

	int getCallType();

	int getTimeout();

	void setTimeout(int timeout);

	long getCreateMillisTime();

	void setCreateMillisTime(long createTime);

	String getServiceName();

	String getMethodName();

	String[] getParamClassName();

	Object[] getParameters();

	int getMessageType();

	void setAttachment(String name, Object attachment);

	Object getAttachment(String name);
	
	String getVersion();
	
	void setVersion(String version);
	
	long getRequestTime();

	void setPequestTime(long requestTime);
	
}
