/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
import java.io.InputStream;
import java.io.OutputStream;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

public interface Serializer {

	Object deserializeRequest(InputStream is) throws SerializationException;

	void serializeRequest(OutputStream os, Object obj) throws SerializationException;

	Object deserializeResponse(InputStream is) throws SerializationException;

	void serializeResponse(OutputStream os, Object obj) throws SerializationException;
	
	Object proxyRequest(InvokerConfig<?> invokerConfig) throws SerializationException;
	
	InvocationResponse newResponse() throws SerializationException;

	InvocationRequest newRequest(InvokerContext invokerContext) throws SerializationException;
}
