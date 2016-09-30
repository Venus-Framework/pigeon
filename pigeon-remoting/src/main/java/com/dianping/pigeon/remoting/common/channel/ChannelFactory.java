package com.dianping.pigeon.remoting.common.channel;

/**
 * @author qi.yin
 *         2016/09/23  上午10:46.
 */
public interface ChannelFactory<C extends Channel> {

    C createChannel();
}
