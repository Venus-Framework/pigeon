/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.component.invocation;

import java.io.Serializable;

/**
 * 不能修改packagename，修改属性需要注意，确保和之前的dpsf兼容。
 * 
 * @author jianhuihuang
 * @version $Id: DPSFSerializable.java, v 0.1 2013-7-5 上午8:25:21 jianhuihuang
 *          Exp $
 */
public interface InvocationSerializable extends Serializable {

	byte getSerialize();
	
	void setSerialize(byte serialize);

	void setSequence(long seq);

	long getSequence();

	Object getObject();

	Object getContext();

	void setContext(Object context);

}
