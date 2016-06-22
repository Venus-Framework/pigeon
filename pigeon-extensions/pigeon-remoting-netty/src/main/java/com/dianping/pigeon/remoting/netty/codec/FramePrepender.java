package com.dianping.pigeon.remoting.netty.codec;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * @author qi.yin
 *         2016/06/22  上午10:12.
 */
public class FramePrepender extends OneToOneEncoder {
    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg == null || !(msg instanceof CodecEvent)) {
            return null;
        } else {
            return ((CodecEvent) msg).getBuffer();
        }
    }
}
