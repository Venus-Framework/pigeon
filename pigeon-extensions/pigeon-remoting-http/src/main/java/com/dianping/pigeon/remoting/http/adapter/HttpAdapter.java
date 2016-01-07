package com.dianping.pigeon.remoting.http.adapter;

import javax.servlet.http.HttpServletRequest;
import com.dianping.dpsf.protocol.DefaultRequest;

/**
 * Created by chenchongze on 16/1/5.
 */
public interface HttpAdapter {

    public DefaultRequest convert(HttpServletRequest request) throws Exception;

}
