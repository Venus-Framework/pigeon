package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.log.LoggerLoader;
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
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

/**
 * @author qi.yin
 *         2016/05/10  上午11:00.
 */
public abstract class AbstractDecoder_ extends FrameDecoder implements Decoder_ {

    private static final Logger logger = LoggerLoader.getLogger(AbstractDecoder_.class);

    private byte[] headMsgs = new byte[2];

    @Override
    public Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
            throws IOException {

        Object message = null;

        if (buffer.readableBytes() > 2) {

            buffer.getBytes(buffer.readerIndex(), headMsgs);

            if ((CodecConstants.MAGIC_FIRST == headMsgs[0]
                    && CodecConstants.MAGIC_SECEND == headMsgs[1])) {
                //old protocal
                message = decode0(ctx, channel, buffer);

            } else if (CodecConstants._MAGIC_FIRST == headMsgs[0]
                    && CodecConstants._MAGIC_SECEND == headMsgs[1]) {
                //new protocal
                message = _decode0(ctx, channel, buffer);

            } else {
                throw new IllegalArgumentException("decode invalid message head:" + headMsgs + ", message:"
                        + buffer);
            }

        }

        return message;
    }

    protected Object decode0(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
            throws IOException {
        Object msg = null;
        if (buffer.readableBytes() > CodecConstants.FRONT_LENGTH) {

            long frameLength = buffer.getUnsignedInt(
                    buffer.readerIndex() +
                            CodecConstants.HEAD_LENGTH);

            if (buffer.readableBytes() >= frameLength + CodecConstants.FRONT_LENGTH) {

                //head
                buffer.skipBytes(2);
                byte serialize = buffer.readByte();
                Long sequence = null;
                try {
                    //body length
                    buffer.skipBytes(CodecConstants.BODY_FIELD_LENGTH);
                    //body
                    int bodyLength = (int) (frameLength - CodecConstants.TAIL_LENGTH);
                    ChannelBuffer frame = extractFrame(buffer, buffer.readerIndex(), bodyLength);
                    buffer.readerIndex(buffer.readerIndex() + bodyLength);
                    //tail
                    sequence = buffer.readLong();
                    buffer.skipBytes(3);
                    //deserialize
                    ChannelBufferInputStream is = new ChannelBufferInputStream(frame);

                    msg = deserialize(serialize, is);
                    //afterDecode
                    afterDecode(msg, serialize, is, channel);
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
                        logger.error("[decode0] doFailResponse failed.", t);
                    }
                }
            }
        }
        return msg;
    }

    protected Object _decode0(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) {
        Object msg = null;
        if (buffer.readableBytes() <= CodecConstants._FRONT_LENGTH) {
            return msg;
        }

        int headerLength = (int) buffer.getUnsignedInt(
                buffer.readerIndex() +
                        CodecConstants._HEAD_LENGTH);

        if (buffer.readableBytes() <= CodecConstants._FRONT_LENGTH_ + headerLength) {
            return msg;
        }

        long bodyLength = buffer.getUnsignedInt(
                buffer.readerIndex() +
                        headerLength +
                        CodecConstants._FRONT_LENGTH);

        long dataLength = headerLength + bodyLength;

        if (buffer.readableBytes() >= dataLength + CodecConstants._FRAME_LENGTH) {
            try {
                //head
                buffer.skipBytes(2);
                //version
                buffer.readByte();
                byte serialize = buffer.readByte();
                boolean needChecksum = (serialize & 0x80) == 1;
                serialize = (byte) (serialize & 0x7F);

                int frameLength = (int) (dataLength + CodecConstants._FRONT_LENGTH_);
                //body
                ChannelBuffer frame = extractFrame(buffer, buffer.readerIndex(), frameLength);
                buffer.readerIndex(buffer.readerIndex() + frameLength);
                //tail
                buffer.skipBytes(4);
                //deserialize
                ChannelBufferInputStream is = new ChannelBufferInputStream(frame);

                msg = deserialize(serialize, is);
                //afterDecode
                afterDecode(msg, serialize, is, channel);
            } catch (Throwable e) {

                logger.error("Deserialize failed. host:"
                        + ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress()
                        + "\n" + e.getMessage(), e);
            }
        }

        return msg;
    }

    protected ChannelBuffer extractFrame(ChannelBuffer buffer, int index, int length) {
        ChannelBuffer frame = buffer.factory().getBuffer(length);
        frame.writeBytes(buffer, index, length);
        return frame;
    }

    private Object afterDecode(Object msg, byte serialize, ChannelBufferInputStream is, Channel channel) throws IOException {
        int available = is.available();

        if (msg instanceof InvocationSerializable) {

            InvocationSerializable msg_ = (InvocationSerializable) msg;
            int msgType = msg_.getMessageType();

            if (msgType == Constants.MESSAGE_TYPE_SERVICE && available > 0) {
                msg_.setSize(available + 3);
            }

            msg_.setSerialize(serialize);
        }

        long receiveTime = System.currentTimeMillis();
        doInitMsg(msg, channel, receiveTime);
        return msg;
    }

    protected abstract Object deserialize(byte serializerType, InputStream is);

    protected abstract Object doInitMsg(Object message, Channel channel, long receiveTime);

    protected abstract void doFailResponse(Channel channel, InvocationResponse response);
}
