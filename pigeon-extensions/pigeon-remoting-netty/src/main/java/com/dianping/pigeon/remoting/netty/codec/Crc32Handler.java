package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.config.CodecConfig;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.CompositeChannelBuffer;
import org.jboss.netty.channel.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.zip.Adler32;

/**
 * @author qi.yin
 *         2016/06/14  下午11:40.
 */
public class Crc32Handler extends SimpleChannelHandler {

    private static final Logger logger = LoggerLoader.getLogger(Crc32Handler.class);

    private static ThreadLocal<Adler32> adler32s = new ThreadLocal<Adler32>();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() == null ||
                !(e.getMessage() instanceof CodecEvent)) {
            return;
        }

        CodecEvent codecEvent = (CodecEvent) e.getMessage();

        if (codecEvent.isUnified()) {

            if (doUnChecksum(e.getChannel(), codecEvent)) {
                Channels.fireMessageReceived(e.getChannel(), codecEvent, e.getRemoteAddress());
            }

        } else {
            ctx.sendUpstream(e);
        }

    }


    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (!(e instanceof MessageEvent)) {
            return;
        }

        MessageEvent evt = (MessageEvent) e;
        if (evt.getMessage() instanceof CodecEvent) {
            return;
        }

        CodecEvent codecEvent = (CodecEvent) evt;
        if (codecEvent.isUnified()) {
            ChannelBuffer buffer = doChecksum(e.getChannel(), codecEvent);
            codecEvent.setFrameBuffer(buffer);

            Channels.write(evt.getChannel(), codecEvent, evt.getRemoteAddress());
        } else {
            ctx.sendDownstream(e);
        }
    }

    private boolean doUnChecksum(Channel channel, CodecEvent codecEvent) {

        ChannelBuffer frame = codecEvent.getFrameBuffer();

        byte command = frame.getByte(frame.readerIndex() +
                CodecConstants._FRONT_COMMAND_LENGTH);

        if ((command & 0x80) == 0x80) {
            int frameLength = frame.readableBytes();

            int dataLength = frameLength - CodecConstants._TAIL_LENGTH;
            ChannelBuffer buffer = frame.factory().getBuffer(dataLength);
            buffer.writeBytes(frame, frame.readerIndex(), dataLength);

            codecEvent.setIsChecksum(true);

            Adler32 adler32 = adler32s.get();
            if (adler32 == null) {
                adler32 = new Adler32();
                adler32s.set(adler32);
            }
            adler32.reset();
            adler32.update(buffer.array(), 0, dataLength);

            int checksum = (int) adler32.getValue();
            int _checksum = frame.getInt(dataLength);

            if (checksum == _checksum) {
                int totalLength = buffer.getByte(CodecConstants._HEAD_LENGTH);
                buffer.setInt(CodecConstants._HEAD_LENGTH, totalLength - CodecConstants._TAIL_LENGTH);

                codecEvent.setFrameBuffer(buffer);
            } else {
                String host = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
                logger.error("Checksum failed. data from host:" + host);
                return false;
            }
        }
        return true;
    }

    private ChannelBuffer doChecksum(Channel channel, CodecEvent codecEvent) {
        ChannelBuffer frame = codecEvent.getFrameBuffer();

        boolean isChecksum = CodecConfig.isChecksum();

        int command = frame.getByte(CodecConstants._FRONT_COMMAND_LENGTH);
        int frameLength = frame.readableBytes();

        if (isChecksum) {
            command = command | 0x80;
            frame.writerIndex(CodecConstants._FRONT_COMMAND_LENGTH);
            frame.writeByte(command);
            frame.writeInt(frameLength + CodecConstants._TAIL_LENGTH);

            //checksum
            Adler32 adler32 = adler32s.get();
            if (adler32 == null) {
                adler32 = new Adler32();
                adler32s.set(adler32);
            }
            adler32.reset();

            if (frame instanceof CompositeChannelBuffer) {
                CompositeChannelBuffer compositeBuffer = (CompositeChannelBuffer) frame;
                ByteBuffer[] bufs = compositeBuffer.toByteBuffers();

                for (ByteBuffer buf : bufs) {
                    adler32.update(buf.array());
                }
            } else {
                adler32.update(frame.array(), 0, frameLength);
            }

            long checksum = adler32.getValue();

            frame.writerIndex(frameLength);
            frame.writeInt((int) checksum);
        } else {
            command = command & 0x7f;
            frame.writerIndex(CodecConstants._FRONT_COMMAND_LENGTH);
            frame.writeByte(command);
            frame.writerIndex(frameLength);
        }

        return frame;
    }

}
