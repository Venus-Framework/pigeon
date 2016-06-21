package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

import java.net.InetSocketAddress;
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
                !(e.getMessage() instanceof DataPackage)) {
            return;
        }

        DataPackage dataPackage = (DataPackage) e.getMessage();

        if (dataPackage.isUnified()) {

            if (doChecksum(e.getChannel(), dataPackage)) {
                Channels.fireMessageReceived(e.getChannel(), dataPackage, e.getRemoteAddress());
            }

        } else {
            ctx.sendUpstream(e);
        }

    }


    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleDownstream(ctx, e);
    }

    private boolean doChecksum(Channel channel, DataPackage dataPackage) {

        ChannelBuffer frame = dataPackage.getFrameBuffer();

        byte command = frame.getByte(frame.readerIndex() +
                CodecConstants._FRONT_COMMAND_LENGTH);

        if ((command & 0x80) == 0x80) {
            int frameLength = frame.readableBytes();

            int dataLength = frameLength - CodecConstants._TAIL_LENGTH;
            ChannelBuffer buffer = frame.factory().getBuffer(dataLength);
            buffer.writeBytes(frame, frame.readerIndex(), dataLength);

            dataPackage.setIsChecksum(true);

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

                dataPackage.setFrameBuffer(buffer);
            } else {
                String host = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
                logger.error("Checksum failed. data from host:" + host);
                return false;
            }
        }
        return true;
    }

}
