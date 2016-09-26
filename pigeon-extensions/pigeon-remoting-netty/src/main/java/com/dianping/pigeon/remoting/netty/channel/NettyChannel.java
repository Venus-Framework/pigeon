package com.dianping.pigeon.remoting.netty.channel;

import com.dianping.pigeon.remoting.common.channel.Channel;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import org.jboss.netty.channel.ChannelFuture;


/**
 * @author qi.yin
 *         2016/09/23  上午10:29.
 */
public interface NettyChannel extends Channel {

    ChannelFuture write0(Object message) throws NetworkException;

}
