/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.rpc.protobuf;

import java.util.List;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.rpc.CodecException;
import com.dianping.pigeon.rpc.RpcMarshaller;
import com.dianping.pigeon.serialize.SerializerFactory;
import com.google.protobuf.Message;

public class ProtobufRpcMarshaller implements RpcMarshaller<Message> {

	@Override
	public DefaultRequest marshalRequest(Message message) throws CodecException {
		ProtobufRpcProtos.ProtobufRequest protobufRequest = (ProtobufRpcProtos.ProtobufRequest) message;
		DefaultRequest request = new DefaultRequest();
		request.setSequence(protobufRequest.getSeq());
		request.setCallType(protobufRequest.getCallType());
		request.setTimeout(protobufRequest.getTimeout());
		request.setVersion(protobufRequest.getVersion());
		request.setMessageType(protobufRequest.getMessageType());
		request.setMethodName(protobufRequest.getMethodName());
		request.setSerialize(SerializerFactory.SERIALIZE_PROTOBUF);
		request.setServiceName(protobufRequest.getUrl());
		List<String> params = protobufRequest.getParametersList();
		request.setParameters(params.toArray());

		return request;
	}

	@Override
	public Message unmarshalRequest(DefaultRequest request) throws CodecException {
		ProtobufRpcProtos.ProtobufRequest.Builder builder = ProtobufRpcProtos.ProtobufRequest.newBuilder()
				.setSeq(request.getSequence()).setMessageType(request.getMessageType())
				.setCallType(request.getCallType()).setTimeout(request.getTimeout()).setUrl(request.getServiceName())
				.setMethodName(request.getMethodName()).setVersion(request.getVersion());
		if (request.getParameters() != null) {
			for (int i = 0; i < request.getParameters().length; i++) {
				builder.setParameters(i, "" + request.getParameters()[i]);
			}
		}

		ProtobufRpcProtos.ProtobufRequest protobufRequest = builder.build();
		return protobufRequest;
	}

	@Override
	public DefaultResponse marshalResponse(Message message) throws CodecException {
		ProtobufRpcProtos.ProtobufResponse protobufResponse = (ProtobufRpcProtos.ProtobufResponse) message;
		DefaultResponse response = new DefaultResponse(protobufResponse.getMessageType(),
				SerializerFactory.SERIALIZE_PROTOBUF);
		response.setSequence(protobufResponse.getSeq());
		response.setReturn(protobufResponse.getResponseMessage());
		response.setCause(protobufResponse.getErrorMessage());

		return null;
	}

	@Override
	public Message unmarshalResponse(DefaultResponse response) throws CodecException {
		ProtobufRpcProtos.ProtobufResponse.Builder builder = ProtobufRpcProtos.ProtobufResponse.newBuilder()
				.setErrorMessage(response.getCause()).setMessageType(response.getMessageType())
				.setSeq(response.getSequence()).setResponseMessage("" + response.getReturn());

		ProtobufRpcProtos.ProtobufResponse protobufResponse = builder.build();
		return protobufResponse;
	}

}
