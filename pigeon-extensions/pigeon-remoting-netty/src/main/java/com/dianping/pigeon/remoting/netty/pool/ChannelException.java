package com.dianping.pigeon.remoting.netty.pool;

/**
 * @author qi.yin
 *         2016/07/26  上午9:43.
 */
public class ChannelException extends Exception {

    private static final long serialVersionUID = -1L;

    public ChannelException() {
        super();
    }

    public ChannelException(String message) {
        super(message);
    }

    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }

}