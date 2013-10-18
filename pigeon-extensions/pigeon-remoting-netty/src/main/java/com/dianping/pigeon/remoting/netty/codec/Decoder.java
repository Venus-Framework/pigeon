/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.codec;

import java.io.IOException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

public interface Decoder {

	Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws IOException, ClassNotFoundException;

}
