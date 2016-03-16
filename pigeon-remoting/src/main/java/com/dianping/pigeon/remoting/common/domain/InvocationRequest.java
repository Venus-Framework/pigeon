/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.domain;

import java.io.Serializable;
import java.util.Map;

public interface InvocationRequest extends InvocationSerializable {

	void setCallType(int callType);

	int getCallType();

	int getTimeout();

	void setTimeout(int timeout);

	long getCreateMillisTime();

	void setCreateMillisTime(long createTime);

	String getServiceName();

	void setServiceName(String serviceName);

	String getMethodName();

	void setMethodName(String methodName);

	String[] getParamClassName();

	Object[] getParameters();

	int getMessageType();

	void setMessageType(int messageType);

	String getVersion();

	String getApp();

	Map<String, Serializable> getGlobalValues();

	void setGlobalValues(Map<String, Serializable> globalValues);

	Map<String, Serializable> getRequestValues();

	void setRequestValues(Map<String, Serializable> requestValues);

}
