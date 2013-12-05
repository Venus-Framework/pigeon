/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.rpc;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public interface RpcMarshaller<T> {

	DefaultRequest marshalRequest(T message) throws CodecException;

	T unmarshalRequest(DefaultRequest request) throws CodecException;

	DefaultResponse marshalResponse(T message) throws CodecException;

	T unmarshalResponse(DefaultResponse response) throws CodecException;

}
