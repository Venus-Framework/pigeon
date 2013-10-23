/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.component.invocation;

/**
 * 不能修改packagename，修改属性需要注意，确保和之前的dpsf兼容。
 * 
 * @author jianhuihuang
 * @version $Id: DPSFResponse.java, v 0.1 2013-6-17 下午6:04:15 jianhuihuang Exp $
 */
public interface InvocationResponse extends InvocationSerializable {

	void setMessageType(int messageType);

	int getMessageType();

	String getCause();

	Object getReturn();

	void setReturn(Object obj);
}
