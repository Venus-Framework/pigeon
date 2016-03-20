package com.dianping.pigeon.registry.exception;

/**
 * Created by chenchongze on 16/3/10.
 */
public class RegionException extends Exception {

    public RegionException(String msg) {
        super(msg);
    }

    public RegionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RegionException(Throwable cause) {
        super(cause);
    }
}
