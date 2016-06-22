package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.listener.ServiceChangeListener;
import com.google.common.collect.Lists;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.inf.octo.mns.model.ServiceListRequest;
import com.sankuai.sgagent.thrift.model.SGService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchongze on 16/5/26.
 */
public class MnsEventNotifier {

    private static ServiceChangeListener serviceChangeListener = new DefaultServiceChangeListener();

    private static List<IServiceListChangeListener> serviceListChangeListeners = Lists.newArrayList();

    public static void eventReceived(MnsEvent event) throws Exception {

        if(event != null) {
            addressChanged();
        } else {
            weightChanged();
        }
    }

    public static void eventReceived(ServiceListRequest req,
                                     List<SGService> oldList,
                                     List<SGService> newList,
                                     List<SGService> addList,
                                     List<SGService> deletedList,
                                     List<SGService> modifiedList) throws Exception {
        String remoteAppkey = req.getRemoteAppkey();

        //servicename的对应关系还未讨论清楚

        //newList 通知addressChanged(pathInfo);

        //modifiedList 检查修改的字段，通知不同的通知器

        //old本地有缓存，用不到； add delete也暂时用不到

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
