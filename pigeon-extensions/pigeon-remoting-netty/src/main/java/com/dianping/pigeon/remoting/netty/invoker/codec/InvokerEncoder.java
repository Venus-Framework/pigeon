package com.dianping.pigeon.remoting.netty.invoker.codec;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.netty.codec.AbstractEncoder;
import com.dianping.pigeon.remoting.netty.codec.NettyCodecUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author qi.yin
 *         2016/06/21  下午3:32.
 */
public class InvokerEncoder extends AbstractEncoder {

    public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        NettyCodecUtils.setAttachment(ctx, Constants.ATTACHMENT_RETRY, msg);
        Object[] message = (Object[]) msg;
        Object encoded = super.encode(ctx, channel, message[0]);
        return encoded;
    }

    @Override
    public void doFailResponse(Channel channel, InvocationResponse response) {
        Channels.fireMessageReceived(channel, response);
    }

    @Override
    public void serialize(byte serializerType, OutputStream os, Object obj, Channel channel)
            throws IOException {
        SerializerFactory.getSerializer(serializerType).serializeRequest(os, obj);
    }
}
