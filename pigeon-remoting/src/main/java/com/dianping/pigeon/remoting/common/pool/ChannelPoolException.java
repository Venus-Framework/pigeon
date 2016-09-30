package com.dianping.pigeon.remoting.common.pool;

import com.dianping.pigeon.remoting.common.exception.RpcException;

/**
 * @author qi.yin
 *         2016/07/25  下午4:08.
 */
public class ChannelPoolException extends RpcException {

    private static final long serialVersionUID = -1L;

    public ChannelPoolException() {
        super();
    }

    public ChannelPoolException(String message) {
        super(message);
    }

    public ChannelPoolException(String message, Throwable cause) {
        super(message, cause);
    }

}