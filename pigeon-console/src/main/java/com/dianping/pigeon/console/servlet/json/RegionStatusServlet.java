package com.dianping.pigeon.console.servlet.json;

import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

import javax.servlet.Servlet;

/**
 * Created by chenchongze on 16/4/1.
 */
public class RegionStatusServlet extends ServiceServlet {

    public RegionStatusServlet(ServerConfig serverConfig, int port) {
        super(serverConfig, port);
    }


}
