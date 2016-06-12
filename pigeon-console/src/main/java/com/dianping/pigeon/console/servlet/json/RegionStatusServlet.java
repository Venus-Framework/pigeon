package com.dianping.pigeon.console.servlet.json;

import com.dianping.pigeon.console.domain.RegionStatus;
import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.remoting.invoker.route.region.RegionPolicyManager;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.google.common.collect.Maps;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by chenchongze on 16/4/1.
 */
public class RegionStatusServlet extends ServiceServlet {

    private final RegionPolicyManager regionPolicyManager = RegionPolicyManager.INSTANCE;

    public RegionStatusServlet(ServerConfig serverConfig, int port) {
        super(serverConfig, port);
    }

    @Override
    protected boolean initServicePage(HttpServletRequest request, HttpServletResponse response) throws IOException {

        RegionStatus regionStatus = new RegionStatus();
        boolean isEnableRegionPolicy = regionPolicyManager.isEnableRegionPolicy();

        if (isEnableRegionPolicy) {
            regionStatus.setRegionPolicyEnabled(Boolean.TRUE);
            String localRegionName = regionPolicyManager.getLocalRegion().getName();
            regionStatus.setLocalRegion(localRegionName);
            regionStatus.setRegionInfos(configManager.getStringValue(regionPolicyManager.KEY_REGIONINFO));
            regionStatus.setRegionPrefer(
                    configManager.getStringValue(regionPolicyManager.KEY_REGION_PREFER_BASE + localRegionName));
        }

        this.model = regionStatus;

        return true;
    }

    @Override
    public String getView() {
        return "RegionStatus.ftl";
    }

    @Override
    public String getContentType() {
        return "application/json; charset=UTF-8";
    }

}
