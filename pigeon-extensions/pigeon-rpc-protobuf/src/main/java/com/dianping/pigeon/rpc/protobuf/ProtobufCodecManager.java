/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.rpc.protobuf;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.rpc.CodecException;
import com.dianping.pigeon.rpc.CodecManager;

public class ProtobufCodecManager implements CodecManager {

	@Override
	public DefaultRequest decodeRequest(Object obj) throws CodecException {

		return null;
	}

	@Override
	public Object encodeRequest(DefaultRequest request) throws CodecException {
		ProtobufRequestProtos.ProtobufRequest protobufRequest = ProtobufRequestProtos.ProtobufRequest.newBuilder()
				.setCallType(request.getCallType()).setMessageType(request.getMessageType())
				.setMethodName(request.getMethodName()).setSeq(request.getSequence()).setTimeout(request.getTimeout())
				.build();
		return protobufRequest;
	}

	@Override
	public DefaultResponse decodeResponse(Object obj) throws CodecException {
		return null;
	}

	@Override
	public Object encodeResponse(DefaultResponse response) throws CodecException {
		return null;
	}

}
