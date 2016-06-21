package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.listener.ServiceChangeListener;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.inf.octo.mns.model.ServiceListRequest;

import java.util.ArrayList;

/**
 * Created by chenchongze on 16/5/26.
 */
public class MnsEventNotifier {

    private static ServiceChangeListener serviceChangeListener = new DefaultServiceChangeListener();

    public static void eventReceived(MnsEvent event) throws Exception {

        if(event != null) {
            addressChanged();
        } else {
            weightChanged();
        }
    }

    private static void addressChanged() {
        serviceChangeListener.onServiceHostChange("fakeServiceName", new ArrayList<String[]>());
    }

    private static void weightChanged() {
        serviceChangeListener.onHostWeightChange("fakeHost", 1);
    }

    public static void main(String[] args) {
        ServiceListRequest serviceListRequest = new ServiceListRequest();
        serviceListRequest.setRemoteAppkey("remoteAppkey");
        serviceListRequest.setLocalAppkey("localAppkey");
        serviceListRequest.setProtocol("thrift");
        MnsInvoker.addServiceListener(serviceListRequest, new DefaultServiceListChangeListener());
    }
}
