/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.component.invocation;

/**
 * 不能修改packagename，修改属性需要注意，确保和之前的dpsf兼容。
 * 
 * @author jianhuihuang
 * @version $Id: DPSFRequest.java, v 0.1 2013-6-17 下午6:04:30 jianhuihuang Exp $
 */
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
	
}
