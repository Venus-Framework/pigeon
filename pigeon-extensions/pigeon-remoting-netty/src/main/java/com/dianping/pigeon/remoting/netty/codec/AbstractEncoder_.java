package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.compress.Compress;
import com.dianping.pigeon.compress.GZipCompress;
import com.dianping.pigeon.compress.SnappyCompress;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.config.CodecConfig;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationSerializable;
import com.dianping.pigeon.remoting.common.domain.generic.CompressType;
import com.dianping.pigeon.remoting.common.domain.generic.UnifiedInvocation;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Adler32;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;
import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

/**
 * @author qi.yin
 *         2016/06/07  上午11:07.
 */
public abstract class AbstractEncoder_ extends OneToOneEncoder implements Encoder_ {

    private static final Logger logger = LoggerLoader.getLogger(AbstractEncoder_.class);

    private static Adler32 adler32 = new Adler32();

    private static Compress gZipCompress = new GZipCompress();

    private static Compress snappyCompress = new SnappyCompress();

    public abstract void serialize(byte serializer, OutputStream os, Object obj, Channel channel) throws IOException;

    @Override
    public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws IOException {
        if (msg instanceof InvocationSerializable) {

            InvocationSerializable _msg = (InvocationSerializable) msg;
            try {

                if (msg instanceof UnifiedInvocation) {
                    //new protocal
                    return _encode0(ctx, channel, (UnifiedInvocation) _msg);
                }

                //old protocal
                return encode0(ctx, channel, _msg);

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
            throw new SerializationException("invalid message format");
        }
    }

    protected Object encode0(ChannelHandlerContext ctx, Channel channel, InvocationSerializable msg)
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

    protected Object _encode0(ChannelHandlerContext ctx, Channel channel, UnifiedInvocation msg)
            throws IOException {

        OutputStream os = new ChannelBufferOutputStream(dynamicBuffer(CodecConstants.ESTIMATED_LENGTH,
                channel.getConfig().getBufferFactory()));

        serialize(msg.getSerialize(), os, msg, channel);

        ChannelBuffer head = channel.getConfig().getBufferFactory().getBuffer(8);
        //magic
        head.writeBytes(CodecConstants._MAGIC);
        //version
        head.writeByte(msg.getProtocalVersion());
        //serialize
        byte serialize = SerializerFactory.convertToUnifiedSerialize(msg.getSerialize());
        //serialize
        head.writeByte(serialize);
        //totalLength
        head.writeInt(Integer.MAX_VALUE);

        //body
        ChannelBuffer frame = ((ChannelBufferOutputStream) os).buffer();

        frame = doAfter(channel, head, frame);

        return wrappedBuffer(head, frame);
    }

    protected ChannelBuffer doAfter(Channel channel, ChannelBuffer head, ChannelBuffer frame)
            throws IOException {
        boolean isChecksum = CodecConfig.isChecksum();
        //compress
        frame = doCompress(channel, head, frame, isChecksum);
        //isChecksum
        doChecksum(head, frame, isChecksum);

        return frame;
    }

    private ChannelBuffer doCompress(Channel channel, ChannelBuffer head, ChannelBuffer frame, boolean isChecksum)
            throws IOException {

        int command = head.getByte(CodecConstants._FRONT_COMMAND_LENGTH);
        //compress
        ChannelBuffer result = frame;
        int frameLength = frame.readableBytes();

        if (CodecConfig.isCompress(frameLength)) {
            CompressType compressType = CodecConfig.getCompressType();

            switch (compressType) {
                case None:
                    command = command | 0x00;
                case Snappy:
                    command = command | 0x20;
                    result = doCompress0(channel, frame, frameLength, snappyCompress, isChecksum);
                    break;
                case Gzip:
                    command = command | 0x40;
                    result = doCompress0(channel, frame, frameLength, gZipCompress, isChecksum);
                    break;
            }
        } else {
            command = command | 0x00;
        }

        head.writerIndex(CodecConstants._FRONT_COMMAND_LENGTH);
        head.writeByte(command);
        return result;
    }

    private ChannelBuffer doCompress0(Channel channel, ChannelBuffer frame, int frameLength, Compress compress, boolean isChecksum)
            throws IOException {
        ChannelBuffer result;
        byte[] in = new byte[frameLength - CodecConstants._HEAD_FIELD_LENGTH];

        frame.getBytes(CodecConstants._HEAD_FIELD_LENGTH, in, 0,
                frameLength - CodecConstants._HEAD_FIELD_LENGTH);

        byte[] out = compress.compress(in);
        byte[] lengthBuf = new byte[CodecConstants._HEAD_FIELD_LENGTH];
        frame.getBytes(0, lengthBuf, 0, lengthBuf.length);

        int length = out.length + lengthBuf.length;
        length = isChecksum ? length + 4 : length;
        result = dynamicBuffer(length, channel.getConfig().getBufferFactory());
        result.writeBytes(lengthBuf);
        result.writeBytes(out);
        return result;
    }

    private void doChecksum(ChannelBuffer head, ChannelBuffer frame, boolean isChecksum) {
        int command = head.getByte(CodecConstants._FRONT_COMMAND_LENGTH);
        int frameLength = frame.readableBytes();

        if (isChecksum) {
            command = command | 0x80;
            head.writerIndex(CodecConstants._FRONT_COMMAND_LENGTH);
            head.writeByte(command);
            head.writeInt(frameLength + CodecConstants._TAIL_LENGTH);
            //checksum
            adler32.reset();
            adler32.update(head.array());
            adler32.update(frame.array(), 0, frameLength);
            long checksum = adler32.getValue();

            frame.writeInt((int) checksum);
        } else {
            command = command & 0x7f;
            head.writerIndex(CodecConstants._FRONT_COMMAND_LENGTH);
            head.writeByte(command);
            head.writeInt(frameLength);
        }

    }

    public abstract void doFailResponse(Channel channel, InvocationResponse response);

}
