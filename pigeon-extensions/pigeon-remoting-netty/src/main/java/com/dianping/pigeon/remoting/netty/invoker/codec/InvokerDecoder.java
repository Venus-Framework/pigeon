package com.dianping.pigeon.remoting.netty.invoker.codec;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractDecoder;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;

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
    public void doFailResponse(Channel channel, InvocationResponse response) {
        List<InvocationResponse> responses = new ArrayList<InvocationResponse>();
        responses.add(response);
        Channels.fireMessageReceived(channel, responses);
    }

    @Override
    public Object deserialize(byte serializerType, InputStream is) {
        Object decoded = SerializerFactory.getSerializer(serializerType).deserializeResponse(is);
        return decoded;
    }
}
