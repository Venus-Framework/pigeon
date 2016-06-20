package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.compress.Compress;
import com.dianping.pigeon.compress.GZipCompress;
import com.dianping.pigeon.compress.SnappyCompress;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;

import java.io.IOException;

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
                !(e.getMessage() instanceof DataPackage)) {
            return;
        }

        DataPackage dataPackage = (DataPackage) e.getMessage();

        if (dataPackage.isUnified()) {

            ChannelBuffer buffer = doCompress(e.getChannel(), dataPackage);
            dataPackage.setFrameBuffer(buffer);

            Channels.fireMessageReceived(e.getChannel(), dataPackage, e.getRemoteAddress());
        } else {
            ctx.sendUpstream(e);
        }
    }

    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleDownstream(ctx, e);
    }

    private ChannelBuffer doCompress(Channel channel, DataPackage dataPackage)
            throws IOException {
        ChannelBuffer frame = dataPackage.getFrameBuffer();

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
                dataPackage.setIsCompress(true);
                break;
            case 0x40:
                in = new byte[compressLength];
                frame.getBytes(frame.readerIndex() + CodecConstants._FRONT_LENGTH, in);
                out = gZipCompress.unCompress(in);
                dataPackage.setIsCompress(true);
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


}
