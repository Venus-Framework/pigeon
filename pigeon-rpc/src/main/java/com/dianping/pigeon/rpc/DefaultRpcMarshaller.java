/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.rpc;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.component.invocation.InvocationSerializable;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public class DefaultRpcMarshaller implements RpcMarshaller<InvocationSerializable> {

	@Override
	public DefaultRequest marshalRequest(InvocationSerializable message) throws CodecException {
		return (DefaultRequest) message;
	}

	@Override
	public InvocationSerializable unmarshalRequest(DefaultRequest request) throws CodecException {
		return request;
	}

	@Override
	public DefaultResponse marshalResponse(InvocationSerializable message) throws CodecException {
		return (DefaultResponse) message;
	}

	@Override
	public InvocationSerializable unmarshalResponse(DefaultResponse response) throws CodecException {
		return response;
	}

}
