package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationSerializable;
import com.dianping.pigeon.remoting.common.domain.generic.UnifiedInvocation;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import java.io.IOException;
import java.io.OutputStream;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;
import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

/**
 * @author qi.yin
 *         2016/06/21  上午9:55.
 */

public abstract class AbstractEncoder__ extends OneToOneEncoder {

    private static final Logger logger = LoggerLoader.getLogger(AbstractEncoder__.class);

    public abstract void serialize(byte serializer, OutputStream os, Object obj, Channel channel) throws IOException;

    @Override
    public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof InvocationSerializable) {

            InvocationSerializable _msg = (InvocationSerializable) msg;
            try {

                ChannelBuffer frame;
                CodecEvent codecEvent;

                if (msg instanceof UnifiedInvocation) {
                    frame = _doEncode(channel, (UnifiedInvocation) _msg);
                    codecEvent = new CodecEvent(frame, true);
                    return codecEvent;
                }

                frame = doEncode(channel, _msg);
                codecEvent = new CodecEvent(frame, false);

                return codecEvent;
            } catch (IOException e) {
                SerializationException se = new SerializationException(e);

                try {
                    doFailResponse(channel, ProviderUtils.createThrowableResponse(_msg.getSequence(),
                            _msg.getSerialize(), se));
                } catch (Throwable t) {
                }

                logger.error(e.getMessage(), se);
                throw se;
            }

        } else {
            throw new SerializationException("Invalid message format");
        }
    }

    protected ChannelBuffer doEncode(Channel channel, InvocationSerializable msg)
            throws IOException {
        OutputStream os = new ChannelBufferOutputStream(dynamicBuffer(CodecConstants.ESTIMATED_LENGTH,
                channel.getConfig().getBufferFactory()));

        serialize(msg.getSerialize(), os, msg, channel);

        ChannelBuffer head = channel.getConfig().getBufferFactory().getBuffer(CodecConstants.FRONT_LENGTH);

        //magic
        head.writeBytes(CodecConstants.MAGIC);
        //serialize
        head.writeByte(msg.getSerialize());

        //body
        ChannelBuffer body = ((ChannelBufferOutputStream) os).buffer();
        //sequence
        body.writeLong(msg.getSequence());
        //expand
        body.writeBytes(CodecConstants.EXPAND);

        head.writeInt(body.readableBytes());

        return wrappedBuffer(head, body);
    }

    protected ChannelBuffer _doEncode(Channel channel, UnifiedInvocation msg)
            throws IOException {

        OutputStream os = new ChannelBufferOutputStream(dynamicBuffer(CodecConstants.ESTIMATED_LENGTH,
                channel.getConfig().getBufferFactory()));

        serialize(msg.getSerialize(), os, msg, channel);

        ChannelBuffer head = channel.getConfig().getBufferFactory().getBuffer(CodecConstants._FRONT_LENGTH_);
        //magic
        head.writeBytes(CodecConstants._MAGIC);
        //version
        head.writeByte(msg.getProtocalVersion());
        //serialize
        byte serialize = SerializerFactory.convertToUnifiedSerialize(msg.getSerialize());
        //serialize
        head.writeByte(serialize);
        //body
        ChannelBuffer frame = ((ChannelBufferOutputStream) os).buffer();
        //totalLength
        head.writeInt(frame.readableBytes());

        return wrappedBuffer(head, frame);
    }

    public abstract void doFailResponse(Channel channel, InvocationResponse response);

}
