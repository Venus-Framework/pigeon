package com.dianping.pigeon.remoting.netty.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * @author qi.yin
 *         2016/05/10  下午7:38.
 */
public interface Decoder_ {

    Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
            throws Exception;

}
