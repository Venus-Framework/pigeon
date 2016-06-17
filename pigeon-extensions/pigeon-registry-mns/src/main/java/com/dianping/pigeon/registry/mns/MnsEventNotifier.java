package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.listener.ServiceChangeListener;

import java.util.ArrayList;

/**
 * Created by chenchongze on 16/5/26.
 */
public class MnsEventNotifier {

    private ServiceChangeListener serviceChangeListener = new DefaultServiceChangeListener();

    public void eventReceived(MnsEvent event) throws Exception {

        if(event != null) {
            addressChanged();
        } else {
            weightChanged();
        }
    }

    private void addressChanged() {
        serviceChangeListener.onServiceHostChange("fakeServiceName", new ArrayList<String[]>());
    }

    private void weightChanged() {
        serviceChangeListener.onHostWeightChange("fakeHost", 1);
    }
}
