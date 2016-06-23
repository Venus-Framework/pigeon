package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.config.CodecConfig;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.*;

import java.net.InetSocketAddress;
import java.util.zip.Adler32;

import static org.jboss.netty.channel.Channels.write;

/**
 * @author qi.yin
 *         2016/06/14  下午11:40.
 */
public class Crc32Handler extends SimpleChannelHandler {

    private static final Logger logger = LoggerLoader.getLogger(Crc32Handler.class);

    private static ThreadLocal<Adler32> adler32s = new ThreadLocal<Adler32>();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() == null || !(e.getMessage() instanceof CodecEvent)) {
            return;
        }

        CodecEvent codecEvent = (CodecEvent) e.getMessage();

        if (codecEvent.isUnified()) {
            if (doUnChecksum(e.getChannel(), codecEvent)) {
                Channels.fireMessageReceived(ctx, codecEvent, e.getRemoteAddress());
            }

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
            ChannelBuffer buffer = doChecksum(e.getChannel(), codecEvent);
            codecEvent.setBuffer(buffer);

            write(ctx, evt.getFuture(), codecEvent, evt.getRemoteAddress());
        } else {
            ctx.sendDownstream(e);
        }
    }

    private boolean doUnChecksum(Channel channel, CodecEvent codecEvent) {

        ChannelBuffer frame = codecEvent.getBuffer();

        byte command = frame.getByte(frame.readerIndex() +
                CodecConstants._FRONT_COMMAND_LENGTH);

        if ((command & 0x80) == 0x80) {
            int frameLength = frame.readableBytes();

            int dataLength = frameLength - CodecConstants._TAIL_LENGTH;
            ChannelBuffer buffer = frame.factory().getBuffer(dataLength);
            buffer.writeBytes(frame, frame.readerIndex(), dataLength);

            codecEvent.setIsChecksum(true);


            int checksum = (int) doChecksum0(buffer, dataLength);
            int _checksum = frame.getInt(dataLength);

            if (checksum == _checksum) {
                int totalLength = buffer.getInt(CodecConstants._HEAD_LENGTH);
                buffer.setInt(CodecConstants._HEAD_LENGTH, totalLength - CodecConstants._TAIL_LENGTH);

                codecEvent.setBuffer(buffer);
            } else {
                String host = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
                logger.error("Checksum failed. data from host:" + host);
                return false;
            }
        }
        return true;
    }

    private ChannelBuffer doChecksum(Channel channel, CodecEvent codecEvent) {
        ChannelBuffer frame = codecEvent.getBuffer();

        boolean isChecksum = CodecConfig.isChecksum();

        int command = frame.getByte(CodecConstants._FRONT_COMMAND_LENGTH);
        int frameLength = frame.readableBytes();

        if (isChecksum) {
            command = command | 0x80;
            //command
            frame.writerIndex(CodecConstants._FRONT_COMMAND_LENGTH);
            frame.writeByte(command);
            //totalLength
            frame.writeInt(frameLength -
                    CodecConstants._FRONT_LENGTH_ +
                    CodecConstants._TAIL_LENGTH);

            frame.writerIndex(frameLength);

            if (!(frame instanceof DynamicChannelBuffer)) {
                ChannelBuffer buffer = frame.factory().getBuffer(frameLength +
                        CodecConstants._TAIL_LENGTH);
                buffer.writeBytes(frame, frame.readerIndex(), frameLength);
                frame = buffer;
            }

            long checksum = doChecksum0(frame, frameLength);

            frame.writeInt((int) checksum);

        } else {
            //command
            command = command & 0x7f;
            frame.writerIndex(CodecConstants._FRONT_COMMAND_LENGTH);
            frame.writeByte(command);
            frame.writerIndex(frameLength);
        }

        return frame;
    }

    private long doChecksum0(ChannelBuffer frame, int frameLength) {
        //checksum
        Adler32 adler32 = adler32s.get();
        if (adler32 == null) {
            adler32 = new Adler32();
            adler32s.set(adler32);
        }
        adler32.reset();

        adler32.update(frame.array(), 0, frameLength);
        return adler32.getValue();
    }


}
