package com.dianping.pigeon.remoting.netty.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.zip.Adler32;

/**
 * @author qi.yin
 *         2016/06/14  下午11:40.
 */
public class Crc32Handler extends SimpleChannelHandler {

    private static ThreadLocal<Adler32> adler32s = new ThreadLocal<Adler32>();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() != null) {
            if (e.getMessage() instanceof DataPackage) {

                DataPackage dataPackage = (DataPackage) e.getMessage();

                if (dataPackage.isUnified()) {
                    ChannelBuffer frame = dataPackage.getFrameBuffer();

                    byte command = frame.getByte(frame.readerIndex() +
                            CodecConstants._FRONT_COMMAND_LENGTH);

                    if ((command & 0x80) == 0x80) {
                        int totalLength = frame.readableBytes();

//                        ChannelBuffer buffer = frame.factory().getBuffer(length);
//                        buffer.writeBytes(frame, index, length);
//                        //ChannelBuffer frame = buffer.slice(index, length);
//                        if (!checksum(frame, totalLength)) {
//                            return msg;
//                        }
                    }

                } else {
                    ctx.sendUpstream(e);
                }
            }
        }
    }

    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleDownstream(ctx, e);
    }

    private boolean checksum(ChannelBuffer frame, int totalLength) {
        int dataLength = totalLength + CodecConstants._HEAD_LENGTH;
        Adler32 adler32 = adler32s.get();
        if (adler32 == null) {
            adler32 = new Adler32();
            adler32s.set(adler32);
        }
        adler32.reset();
        adler32.update(frame.array(), 0, dataLength);

        int checksum = (int) adler32.getValue();
        int _checksum = frame.getInt(dataLength);

        if (checksum != _checksum) {
            return false;
        }
        return true;
    }

}
