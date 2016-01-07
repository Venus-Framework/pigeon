package com.dianping.pigeon.remoting.http.adapter;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chenchongze on 16/1/5.
 */
public interface HttpAdapter {

    public Object convert(HttpServletRequest request) throws Exception;

}
