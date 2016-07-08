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

/**
 * @author qi.yin
 *         2016/06/21  上午9:55.
 */

public abstract class AbstractEncoder extends OneToOneEncoder {

    private static final Logger logger = LoggerLoader.getLogger(AbstractEncoder.class);

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
                } else {
                    frame = doEncode(channel, _msg);
                    codecEvent = new CodecEvent(frame, false);
                }

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
        ChannelBufferOutputStream os = new ChannelBufferOutputStream(dynamicBuffer(CodecConstants.ESTIMATED_LENGTH,
                channel.getConfig().getBufferFactory()));
        //magic
        os.write(CodecConstants.MAGIC);
        //serialize
        os.writeByte(msg.getSerialize());
        //bodyLength
        os.writeInt(Integer.MAX_VALUE);

        serialize(msg.getSerialize(), os, msg, channel);
        //body
        ChannelBuffer frame = os.buffer();
        //sequence
        frame.writeLong(msg.getSequence());
        //expand
        frame.writeBytes(CodecConstants.EXPAND);
        //bodyLength
        frame.setInt(CodecConstants.HEAD_LENGTH, frame.readableBytes() -
                CodecConstants.FRONT_LENGTH);

        return frame;
    }

    protected ChannelBuffer _doEncode(Channel channel, UnifiedInvocation msg)
            throws IOException {

        ChannelBufferOutputStream os = new ChannelBufferOutputStream(dynamicBuffer(CodecConstants.ESTIMATED_LENGTH,
                channel.getConfig().getBufferFactory()));

        //magic
        os.write(CodecConstants._MAGIC);
        os.writeByte(msg.getProtocalVersion());
        //serialize
        byte serialize = SerializerFactory.convertToUnifiedSerialize(msg.getSerialize());
        //serialize
        os.writeByte(serialize);
        //totalLength
        os.writeInt(Integer.MAX_VALUE);

        serialize(msg.getSerialize(), os, msg, channel);

        ChannelBuffer frame = os.buffer();
        //totalLength
        frame.setInt(CodecConstants._HEAD_LENGTH, frame.readableBytes() -
                CodecConstants._FRONT_LENGTH_);

        return frame;
    }

    public abstract void doFailResponse(Channel channel, InvocationResponse response);

}
