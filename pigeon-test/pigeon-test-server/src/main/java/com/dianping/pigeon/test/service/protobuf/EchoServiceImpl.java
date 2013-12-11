/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service.protobuf;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.test.service.protobuf.EchoProtos.EchoRequest;
import com.dianping.pigeon.test.service.protobuf.EchoProtos.EchoResponse;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class EchoServiceImpl implements EchoProtos.EchoService.BlockingInterface {

	Logger logger = LoggerLoader.getLogger(EchoServiceImpl.class);

	@Override
	public EchoResponse echo(RpcController controller, EchoRequest request) throws ServiceException {
		String result = "echo:" + request.getMessage();
		EchoProtos.EchoResponse resp = EchoProtos.EchoResponse.newBuilder().setResult(result).build();
		return resp;
	}
}
