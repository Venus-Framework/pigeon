package com.dianping.pigeon.remoting.netty.invoker.codec;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractDecoder_;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qi.yin
 *         2016/06/27  下午8:14.
 */
public class InvokerDecoder_ extends AbstractDecoder_ {
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
        List<InvocationResponse> respList = new ArrayList<InvocationResponse>();
        respList.add(response);
        Channels.fireMessageReceived(channel, respList);
    }

    @Override
    public Object deserialize(byte serializerType, InputStream is) {
        Object decoded = SerializerFactory.getSerializer(serializerType).deserializeResponse(is);
        return decoded;
    }
}
