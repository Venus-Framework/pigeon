package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.compress.Compress;
import com.dianping.pigeon.compress.GZipCompress;
import com.dianping.pigeon.compress.SnappyCompress;
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
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.zip.Adler32;

/**
 * @author qi.yin
 *         2016/06/07  上午11:06.
 */
public abstract class AbstractDecoder_ extends FrameDecoder implements Decoder_ {

    private static final Logger logger = LoggerLoader.getLogger(AbstractDecoder_.class);

    private byte[] headMsgs = new byte[2];

    private static Adler32 adler32 = new Adler32();

    private static Compress gZipCompress = new GZipCompress();

    private static Compress snappyCompress = new SnappyCompress();

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
                    //after
                    doAfter(msg, serialize, is, channel);
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

    protected Object _decode0(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws IOException {
        Object msg = null;
        if (buffer.readableBytes() <= CodecConstants._FRONT_LENGTH) {
            return msg;
        }

        int totalLength = (int) (buffer.getUnsignedInt(
                buffer.readerIndex() +
                        CodecConstants._HEAD_LENGTH));

        int frameLength = totalLength + CodecConstants._FRONT_LENGTH_;

        if (buffer.readableBytes() >= frameLength) {
            try {
                ChannelBuffer frame = extractFrame(buffer, buffer.readerIndex(), frameLength);
                buffer.readerIndex(buffer.readerIndex() + frameLength);
                //checksum
                byte command = frame.getByte(frame.readerIndex() +
                        CodecConstants._FRONT_COMMAND_LENGTH);
                boolean isChecksum = false;
                if ((command & 0x80) == 0x80) {
                    if (!checksum(frame, totalLength)) {
                        return msg;
                    }
                    isChecksum = true;
                }
                //magic
                frame.skipBytes(CodecConstants._MEGIC_FIELD_LENGTH);
                //version
                frame.readByte();
                //serialize
                byte serialize = (byte) (frame.getByte(frame.readerIndex()) & 0x1f);
                serialize = SerializerFactory.convertToSerialize(serialize);

                //doBefore
                ChannelBuffer frameBody = doBefore(channel, frame, isChecksum);

                ChannelBufferInputStream is = new ChannelBufferInputStream(frameBody);
                //deserialize
                msg = deserialize(serialize, is);
                //doAfter
                doAfter(msg, serialize, is, channel);
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

    private boolean checksum(ChannelBuffer frame, int totalLength) {
        int dataLength = totalLength + CodecConstants._HEAD_LENGTH;
        adler32.reset();
        adler32.update(frame.array(), 0, dataLength);

        int checksum = (int) adler32.getValue();
        int _checksum = frame.getInt(dataLength);

        if (checksum != _checksum) {
            return false;
        }
        return true;
    }

    private ChannelBuffer doCompress(Channel channel, ChannelBuffer frame, boolean isChecksum)
            throws IOException {

        //compact
        short compress = (short) (frame.readByte() & 0x60);
        //totalLength
        int totalLength = frame.readInt();

        int compressLength = totalLength - CodecConstants._HEAD_FIELD_LENGTH;
        compressLength = isChecksum ? compressLength - CodecConstants._TAIL_LENGTH
                : compressLength;

        byte[] in;
        byte[] out = null;
        ChannelBuffer result;

        switch (compress) {
            case 0x00:
                return frame;
            case 0x20:
                in = new byte[compressLength];
                frame.getBytes(frame.readerIndex() + CodecConstants._HEAD_FIELD_LENGTH, in);
                out = snappyCompress.unCompress(in);
                break;
            case 0x40:
                in = new byte[compressLength];
                frame.getBytes(frame.readerIndex() + CodecConstants._HEAD_FIELD_LENGTH, in);
                out = gZipCompress.unCompress(in);
                break;
            case 0x60:
                throw new IllegalArgumentException("Invalid compress value.");
        }

        byte[] lengthBuf = new byte[CodecConstants._HEAD_FIELD_LENGTH];
        frame.getBytes(0, lengthBuf, 0, lengthBuf.length);

        result = channel.getConfig().getBufferFactory().getBuffer(out.length + lengthBuf.length);

        result.writeBytes(lengthBuf);
        result.writeBytes(out);

        return result;
    }

    protected ChannelBuffer doBefore(Channel channel, ChannelBuffer buffer, boolean isChecksum) throws IOException {
        return doCompress(channel, buffer, isChecksum);
    }

    private Object doAfter(Object msg, byte serialize, ChannelBufferInputStream is, Channel channel) throws IOException {
        int available = is.available();

        if (msg instanceof InvocationSerializable) {

            InvocationSerializable msg_ = (InvocationSerializable) msg;
            int msgType = msg_.getMessageType();

            if (msgType == Constants.MESSAGE_TYPE_SERVICE && available > 0) {
                msg_.setSize(available + 3);//error
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
