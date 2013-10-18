/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider.codec;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;

import com.dianping.dpsf.component.DPSFRequest;
import com.dianping.dpsf.component.DPSFResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractDecoder;

public class ProviderDecoder extends AbstractDecoder {

	@Override
	public Object doInitMsg(Object message) {
		if (message == null) {
			return null;
		}
		DPSFRequest request = (DPSFRequest) message;
		if (request.getCreateMillisTime() == 0) {
			request.setCreateMillisTime(System.currentTimeMillis());
		}
		return request;
	}

	@Override
	public void doFailResponse(Channel channel, DPSFResponse response) {
		Channels.write(channel, response);
	}

}
