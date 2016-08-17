package com.dianping.pigeon.registry.mns;

import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;

import java.util.List;

/**
 * Created by chenchongze on 16/6/21.
 */
public class TestServiceListChangeListener implements IServiceListChangeListener {

    @Override
    public void changed(ProtocolRequest req,
                        List<SGService> oldList,
                        List<SGService> newList,
                        List<SGService> addList,
                        List<SGService> deletedList,
                        List<SGService> modifiedList) {
        try {
            if (newList.size() > 0) { //newList 通知addressChanged
                System.out.println("new list begins.");

                for (SGService sgService : newList) {
                    System.out.println(sgService);
                }

                System.out.println("new list ends.");
            }

            if (modifiedList.size() > 0) { // status,weight,fweight,version,role,extend
                System.out.println("modified list begins.");

                for (SGService sgService : modifiedList) {
                    System.out.println(sgService);
                }

                System.out.println("modified list ends.");
            }
        } catch (Throwable e) {
            System.out.println("failed to notify service list change. " + e.getMessage());
        }
    }


}
