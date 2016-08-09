package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.log.LoggerLoader;
import com.google.common.collect.Sets;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * Created by chenchongze on 16/8/9.
 */
public enum ServiceListChangeListenerManager {

    INSTANCE;

    private ServiceListChangeListenerManager() {}

    private final Logger logger = LoggerLoader.getLogger(this.getClass());

    private final Set<ProtocolRequest> protocolRequests = Sets.newConcurrentHashSet();

    private static final int ADD_LISTENER_SUCCESS_FLAG = 0;

    public synchronized void registerListener(ProtocolRequest protocolRequest) {

        if (!protocolRequests.contains(protocolRequest)) {

            if (ADD_LISTENER_SUCCESS_FLAG == MnsInvoker
                    .addServiceListener(protocolRequest, new DefaultServiceListChangeListener())) {
                protocolRequests.add(protocolRequest);
            } else {
                logger.error("add listener failed to " + protocolRequest);
            }

        } else {
            logger.warn("already added listener to " + protocolRequest);
        }

    }
}
