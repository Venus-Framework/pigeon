package com.dianping.pigeon.console.servlet.json;

import com.dianping.pigeon.console.domain.RequestQualityStatus;
import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.remoting.invoker.route.quality.RequestQualityManager;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * Created by chenchongze on 16/6/28.
 */
public class RequestQualityServlet extends ServiceServlet {

    private RequestQualityManager requestQualityManager = RequestQualityManager.INSTANCE;

    public RequestQualityServlet(ServerConfig serverConfig, int port) {
        super(serverConfig, port);
    }

    @Override
    protected boolean initServicePage(HttpServletRequest request, HttpServletResponse response) throws IOException {

        RequestQualityStatus requestQualityStatus = new RequestQualityStatus();
        boolean support = requestQualityManager.isEnableRequestQualityRoute();

        if (support) {
            requestQualityStatus.setSupport(Boolean.TRUE);
            if(!CollectionUtils.isEmpty(requestQualityManager.getAddrReqUrlQualities())) {
                requestQualityStatus.setAddrReqUrlQualities(requestQualityManager.getAddrReqUrlQualities());
            }
        }

        this.model = requestQualityStatus;
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
