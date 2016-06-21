package com.dianping.pigeon.remoting.netty.codec;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * @author qi.yin
 *         2016/06/06  下午5:21.
 */
public interface Encoder_ {

    Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
            throws Exception;

}
