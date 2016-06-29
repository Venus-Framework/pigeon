package com.dianping.pigeon.console.servlet.json;

import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by chenchongze on 16/6/28.
 */
public class RequestQualityServlet extends ServiceServlet {

    public RequestQualityServlet(ServerConfig serverConfig, int port) {
        super(serverConfig, port);
    }

    @Override
    protected boolean initServicePage(HttpServletRequest request, HttpServletResponse response) throws IOException {

        this.model = null;
        return true;
    }

    @Override
    public String getView() {
        return "RequestQuality.ftl";
    }

    @Override
    public String getContentType() {
        return "application/json; charset=UTF-8";
    }

}
