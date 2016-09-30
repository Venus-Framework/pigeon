package com.dianping.pigeon.remoting.netty.invoker.codec;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractEncoder;
import com.dianping.pigeon.remoting.netty.codec.CodecEvent;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qi.yin
 *         2016/06/21  下午3:32.
 */
public class InvokerEncoder extends AbstractEncoder {

    public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        Object encoded = super.encode(ctx, channel, msg);
        return encoded;
    }

    @Override
    public void doFailResponse(ChannelHandlerContext ctx, Channel channel, InvocationResponse response) {
        CodecEvent codecEvent = new CodecEvent();

        codecEvent.setInvocation(response);

        Channels.fireMessageReceived(ctx, codecEvent);
    }

    @Override
    public void serialize(byte serializerType, OutputStream os, Object obj, Channel channel)
            throws IOException {
        SerializerFactory.getSerializer(serializerType).serializeRequest(os, obj);
    }
}
