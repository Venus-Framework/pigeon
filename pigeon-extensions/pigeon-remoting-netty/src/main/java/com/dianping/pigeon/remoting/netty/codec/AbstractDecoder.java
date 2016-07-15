package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationSerializable;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qi.yin
 *         2016/06/21  上午9:55.
 */
public abstract class AbstractDecoder extends OneToOneDecoder {

    private static final Logger logger = LoggerLoader.getLogger(AbstractDecoder.class);

    @Override
    public Object decode(ChannelHandlerContext ctx, Channel channel, Object msg)
            throws Exception {

        if (msg == null || !(msg instanceof List)) {
            return null;
        }

        List<CodecEvent> codecEvents = (List<CodecEvent>) msg;

        List<Object> messages = new ArrayList<Object>(codecEvents.size());

        for (CodecEvent codecEvent : codecEvents) {
            if(!codecEvent.isValid()){
                continue;
            }

            Object message = null;

            if (codecEvent.isUnified()) {
                message = _doDecode(channel, codecEvent);
            } else {
                message = doDecode(channel, codecEvent);
            }

            messages.add(message);
        }

        return messages;
    }

    protected Object doDecode(Channel channel, CodecEvent codecEvent)
            throws IOException {
        Object msg = null;
        ChannelBuffer buffer = codecEvent.getBuffer();
        //head
        buffer.skipBytes(CodecConstants.MEGIC_FIELD_LENGTH);
        byte serialize = buffer.readByte();
        Long sequence = null;

        try {
            //body length
            int totalLength = buffer.readInt();
            int frameLength = totalLength + CodecConstants.FRONT_LENGTH;
            //body
            int bodyLength = (totalLength - CodecConstants.TAIL_LENGTH);
            ChannelBuffer frame = extractFrame(buffer, buffer.readerIndex(), bodyLength);
            buffer.readerIndex(buffer.readerIndex() + bodyLength);
            //tail
            sequence = buffer.readLong();
            buffer.skipBytes(CodecConstants.EXPAND_FIELD_LENGTH);
            //deserialize
            ChannelBufferInputStream is = new ChannelBufferInputStream(frame);

            msg = deserialize(serialize, is);
            //after
            doAfter(channel, msg, serialize, frameLength, codecEvent.getReceiveTime());
        } catch (Throwable e) {
            SerializationException se = new SerializationException(e);

            try {
                if (sequence != null) {
                    doFailResponse(channel, ProviderUtils.createThrowableResponse(sequence.longValue(),
                            serialize, se));
                }

                logger.error("Deserialize failed. host:"
                        + ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress()
                        + "\n" + e.getMessage(), se);

            } catch (Throwable t) {
                logger.error("[doDecode] doFailResponse failed.", t);
            }
        }
        return msg;
    }

    protected Object _doDecode(Channel channel, CodecEvent codecEvent) throws IOException {
        Object msg = null;
        ChannelBuffer buffer = codecEvent.getBuffer();

        try {
            //magic
            buffer.skipBytes(CodecConstants._MEGIC_FIELD_LENGTH);
            //version
            buffer.readByte();
            //serialize
            byte serialize = (byte) (buffer.readByte() & 0x1f);
            serialize = SerializerFactory.convertToSerialize(serialize);

            int totalLength = buffer.readInt();
            int frameLength = totalLength + CodecConstants._FRONT_LENGTH_;

            ChannelBuffer frameBody = extractFrame(buffer, buffer.readerIndex(), totalLength);
            buffer.readerIndex(buffer.readerIndex() + totalLength);

            ChannelBufferInputStream is = new ChannelBufferInputStream(frameBody);
            //deserialize
            msg = deserialize(serialize, is);
            //doAfter
            doAfter(channel, msg, serialize, frameLength, codecEvent.getReceiveTime());
        } catch (Throwable e) {

            logger.error("Deserialize failed. host:"
                    + ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress()
                    + "\n" + e.getMessage(), e);
        }

        return msg;
    }

    protected ChannelBuffer extractFrame(ChannelBuffer buffer, int index, int length) {
        ChannelBuffer frame = buffer.slice(index, length);
        return frame;
    }

    private Object doAfter(Channel channel,
                           Object msg,
                           byte serialize,
                           int frameLength,
                           long receiveTime)
            throws IOException {

        if (msg instanceof InvocationSerializable) {

            InvocationSerializable msg_ = (InvocationSerializable) msg;
            int msgType = msg_.getMessageType();

            if (msgType == Constants.MESSAGE_TYPE_SERVICE && frameLength > 0) {
                msg_.setSize(frameLength);
            }

            msg_.setSerialize(serialize);
        }

        doInitMsg(msg, channel, receiveTime);
        return msg;
    }


    protected abstract Object deserialize(byte serializerType, InputStream is);

    protected abstract Object doInitMsg(Object message, Channel channel, long receiveTime);

    protected abstract void doFailResponse(Channel channel, InvocationResponse response);

}
