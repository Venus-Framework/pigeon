package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.*;

import java.net.InetSocketAddress;

/**
 * @author qi.yin
 *         2016/06/16  下午2:26.
 */
public class CodecHandler extends SimpleChannelHandler {

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() == null ||
                !(e.getMessage() instanceof DataPackage)) {
            return;
        }

        DataPackage dataPackage = (DataPackage) e.getMessage();

        if (dataPackage.isUnified()) {

        } else {

        }
    }

    public Object doCodec(Channel channel, DataPackage dataPackage) {
        ChannelBuffer frame = dataPackage.getFrameBuffer();
        //head
        frame.skipBytes(CodecConstants.MEGIC_FIELD_LENGTH);
        byte serialize = frame.readByte();
        Long sequence = null;
//        try {
//            //body length
//            buffer.skipBytes(CodecConstants.BODY_FIELD_LENGTH);
//            //body
//            int bodyLength = (totalLength - CodecConstants.TAIL_LENGTH);
//            ChannelBuffer frame = extractFrame(buffer, buffer.readerIndex(), bodyLength);
//            buffer.readerIndex(buffer.readerIndex() + bodyLength);
//            //tail
//            sequence = buffer.readLong();
//            buffer.skipBytes(CodecConstants.EXPAND_FIELD_LENGTH);
//            //deserialize
//            ChannelBufferInputStream is = new ChannelBufferInputStream(frame);
//
//            msg = deserialize(serialize, is);
//            //after
//            doAfter(channel, msg, serialize, frameLength);
//        } catch (Throwable e) {
//            SerializationException se = new SerializationException(e);
//
//            try {
//                if (sequence != null) {
//                    doFailResponse(channel, ProviderUtils.createThrowableResponse(sequence.longValue(),
//                            serialize, se));
//                }
//
//                logger.error("Deserialize failed. host:"
//                        + ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress()
//                        + "\n" + e.getMessage(), se);
//
//            } catch (Throwable t) {
//                logger.error("[decode0] doFailResponse failed.", t);
//            }
//        }
        return null;
    }

    public Object _doCodec(Channel channel, DataPackage dataPackage) {
        return null;
    }


}
