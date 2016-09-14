package com.dianping.pigeon.remoting.netty.invoker;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.netty.codec.CodecEvent;
import org.jboss.netty.channel.*;

import java.util.List;

/**
 * @author qi.yin
 *         2016/06/21  下午3:36.
 */
public class NettyClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerLoader.getLogger(NettyClientHandler.class);

    private NettyClient client;

    public NettyClientHandler(NettyClient client) {
        this.client = client;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        List<CodecEvent> codecEvents = (List<CodecEvent>) e.getMessage();

        for (final CodecEvent codecEvent : codecEvents) {

            if (codecEvent.isValid() && codecEvent.getInvocation() != null) {
                client.processResponse((InvocationResponse) codecEvent.getInvocation());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.error("[exceptionCaught] channel exception, will be close. ", e.getCause());
        e.getChannel().close();
    }

}