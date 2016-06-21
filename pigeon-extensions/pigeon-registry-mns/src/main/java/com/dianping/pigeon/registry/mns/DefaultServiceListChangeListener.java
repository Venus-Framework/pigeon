package com.dianping.pigeon.registry.mns;

import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.inf.octo.mns.model.ServiceListRequest;
import com.sankuai.sgagent.thrift.model.SGService;

import java.util.List;

/**
 * Created by chenchongze on 16/6/21.
 */
public class DefaultServiceListChangeListener implements IServiceListChangeListener {

    @Override
    public void changed(ServiceListRequest req,
                        List<SGService> oldList,
                        List<SGService> newList,
                        List<SGService> addList,
                        List<SGService> deletedList,
                        List<SGService> modifiedList) {

    }


}
