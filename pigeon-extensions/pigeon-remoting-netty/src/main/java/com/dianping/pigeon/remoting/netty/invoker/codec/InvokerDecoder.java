package com.dianping.pigeon.remoting.netty.invoker.codec;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractDecoder;
import com.dianping.pigeon.remoting.netty.codec.CodecEvent;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.omg.IOP.Codec;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qi.yin
 *         2016/06/21  下午3:31.
 */
public class InvokerDecoder extends AbstractDecoder {

    @Override
    public Object doInitMsg(Object message, Channel channel, long receiveTime) {
        if (message instanceof InvocationResponse) {
            InvocationResponse response = (InvocationResponse) message;
            response.setCreateMillisTime(receiveTime);
            return response;
        }
        return message;
    }

    @Override
    public void doFailResponse(ChannelHandlerContext ctx, Channel channel, InvocationResponse response) {
        CodecEvent codecEvent = new CodecEvent();

        codecEvent.setInvocation(response);

        Channels.fireMessageReceived(ctx, codecEvent);
    }

    @Override
    public Object deserialize(byte serializerType, InputStream is) {
        Object decoded = SerializerFactory.getSerializer(serializerType).deserializeResponse(is);
        return decoded;
    }
}
