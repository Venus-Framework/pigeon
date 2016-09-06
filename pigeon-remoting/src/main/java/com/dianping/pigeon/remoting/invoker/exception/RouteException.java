package com.dianping.pigeon.remoting.invoker.exception;

import com.dianping.pigeon.remoting.common.exception.RpcException;

/**
 * Created by chenchongze on 16/3/10.
 */
public class RouteException extends RpcException {

	public RouteException() {
		super();
	}

	public RouteException(String msg) {
		super(msg);
	}

	public RouteException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public RouteException(Throwable cause) {
		super(cause);
	}
}
