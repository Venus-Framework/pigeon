package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.log.LoggerLoader;
import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.inf.octo.mns.model.ServiceListRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by chenchongze on 16/6/21.
 */
public class DefaultServiceListChangeListener implements IServiceListChangeListener {

    private static Logger logger = LoggerLoader.getLogger(DefaultServiceListChangeListener.class);

    @Override
    public void changed(ServiceListRequest req,
                        List<SGService> oldList,
                        List<SGService> newList,
                        List<SGService> addList,
                        List<SGService> deletedList,
                        List<SGService> modifiedList) {
        try {
            MnsEventNotifier.eventReceived(req, oldList, newList, addList, deletedList, modifiedList);
        } catch (Exception e) {
            logger.error("failed to notify service list change...", e);
        }
    }


}
