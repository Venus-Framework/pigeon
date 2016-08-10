package com.dianping.pigeon.remoting.provider.exception;

import com.dianping.pigeon.remoting.common.exception.RpcException;

/**
 * Created by chenchongze on 16/7/25.
 */
public class BadRequestException extends RpcException {

    public BadRequestException(String message) {
        super(message);
    }
}
