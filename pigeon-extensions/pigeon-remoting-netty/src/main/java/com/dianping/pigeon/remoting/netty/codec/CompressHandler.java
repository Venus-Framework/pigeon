package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.compress.Compress;
import com.dianping.pigeon.compress.GZipCompress;
import com.dianping.pigeon.compress.SnappyCompress;
import com.dianping.pigeon.remoting.common.config.CodecConfig;
import com.dianping.pigeon.remoting.common.domain.generic.CompressType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

import java.io.IOException;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;
import static org.jboss.netty.channel.Channels.write;

/**
 * @author qi.yin
 *         2016/06/14  下午11:40.
 */
public class CompressHandler extends SimpleChannelHandler {

    private static Compress gZipCompress = new GZipCompress();

    private static Compress snappyCompress = new SnappyCompress();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() == null ||
                !(e.getMessage() instanceof CodecEvent)) {
            return;
        }

        CodecEvent codecEvent = (CodecEvent) e.getMessage();

        if (codecEvent.isUnified()) {

            ChannelBuffer buffer = doUnCompress(e.getChannel(), codecEvent);
            codecEvent.setBuffer(buffer);

            Channels.fireMessageReceived(ctx, codecEvent, e.getRemoteAddress());
        } else {
            ctx.sendUpstream(e);
        }
    }

    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (!(e instanceof MessageEvent)) {
            ctx.sendDownstream(e);
            return;
        }

        MessageEvent evt = (MessageEvent) e;
        if (!(evt.getMessage() instanceof CodecEvent)) {
            ctx.sendDownstream(evt);
            return;
        }

        CodecEvent codecEvent = (CodecEvent) evt.getMessage();
        if (codecEvent.isUnified()) {
            ChannelBuffer buffer = doCompress(e.getChannel(), codecEvent);
            codecEvent.setBuffer(buffer);
            write(ctx, evt.getFuture(), codecEvent, evt.getRemoteAddress());
        } else {
            ctx.sendDownstream(e);
        }
    }

    private ChannelBuffer doUnCompress(Channel channel, CodecEvent codecEvent)
            throws IOException {
        ChannelBuffer frame = codecEvent.getBuffer();

        byte command = frame.getByte(CodecConstants._FRONT_COMMAND_LENGTH);
        //compact
        short compress = (short) (command & 0x60);

        if (compress == 0x00) {
            return frame;
        }

        int totalLength = frame.getInt(frame.readerIndex() + CodecConstants._HEAD_LENGTH);
        int compressLength = totalLength - CodecConstants._HEAD_FIELD_LENGTH;

        byte[] in;
        byte[] out = null;
        ChannelBuffer result;

        switch (compress) {
            case 0x00:
                return frame;
            case 0x20:
                in = new byte[compressLength];
                frame.getBytes(frame.readerIndex() + CodecConstants._FRONT_LENGTH, in);
                out = snappyCompress.unCompress(in);
                codecEvent.setIsCompress(true);
                break;
            case 0x40:
                in = new byte[compressLength];
                frame.getBytes(frame.readerIndex() + CodecConstants._FRONT_LENGTH, in);
                out = gZipCompress.unCompress(in);
                codecEvent.setIsCompress(true);
                break;
            case 0x60:
                throw new IllegalArgumentException("Invalid compress type.");
        }

        int _totalLength = CodecConstants._HEAD_FIELD_LENGTH + out.length;

        result = channel.getConfig().getBufferFactory().getBuffer(
                _totalLength + CodecConstants._FRONT_LENGTH_);

        result.writeBytes(frame, frame.readerIndex(), CodecConstants._HEAD_LENGTH);
        result.writeBytes(frame, _totalLength);
        result.writeBytes(out);

        return result;
    }

    private ChannelBuffer doCompress(Channel channel, CodecEvent codecEvent)
            throws IOException {
        ChannelBuffer frame = codecEvent.getBuffer();
        int command = frame.getByte(CodecConstants._FRONT_COMMAND_LENGTH);
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
                    result = doCompress0(channel, frame, frameLength, snappyCompress);
                    break;
                case Gzip:
                    command = command | 0x40;
                    result = doCompress0(channel, frame, frameLength, gZipCompress);
                    break;
            }
        } else {
            command = command | 0x00;
        }
        int oldWriteIndex = result.writerIndex();
        result.writerIndex(CodecConstants._FRONT_COMMAND_LENGTH);
        result.writeByte(command);
        result.writerIndex(oldWriteIndex);
        return result;
    }

    private ChannelBuffer doCompress0(Channel channel, ChannelBuffer frame,
                                      int frameLength, Compress compress)
            throws IOException {
        ChannelBuffer result;
        int bodyLength = frameLength - CodecConstants._FRONT_LENGTH;
        byte[] in = new byte[bodyLength];

        frame.getBytes(CodecConstants._FRONT_LENGTH, in, 0, bodyLength);

        byte[] out = compress.compress(in);
        byte[] lengthBuf = new byte[CodecConstants._HEAD_FIELD_LENGTH];
        frame.getBytes(0, lengthBuf, 0, lengthBuf.length);

        int totalLength = out.length + lengthBuf.length;
        int _frameLength = totalLength + CodecConstants._FRONT_LENGTH_;
        result = dynamicBuffer(_frameLength, channel.getConfig().getBufferFactory());
        result.writeBytes(frame, frame.readerIndex(), CodecConstants._HEAD_LENGTH);
        result.writeInt(totalLength);
        result.writeBytes(lengthBuf);
        result.writeBytes(out);
        return result;
    }
}
