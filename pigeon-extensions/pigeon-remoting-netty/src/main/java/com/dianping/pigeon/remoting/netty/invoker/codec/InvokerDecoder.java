/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.invoker.codec;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;

import com.dianping.dpsf.component.DPSFResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractDecoder;

public class InvokerDecoder extends AbstractDecoder {

	@Override
	public Object doInitMsg(Object message) {

		return message;
	}

	@Override
	public void doFailResponse(Channel channel, DPSFResponse response) {
		List<DPSFResponse> respList = new ArrayList<DPSFResponse>();
		respList.add(response);
		Channels.fireMessageReceived(channel, respList);
	}

}
