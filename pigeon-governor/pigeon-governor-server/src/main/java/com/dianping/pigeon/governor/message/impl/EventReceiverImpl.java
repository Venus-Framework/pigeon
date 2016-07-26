package com.dianping.pigeon.governor.message.impl;

import com.dianping.pigeon.governor.message.EventReceiver;
import com.dianping.pigeon.governor.message.SenderType;

import java.util.List;
import java.util.Map;

/**
 * Created by shihuashen on 16/7/21.
 */
public class EventReceiverImpl implements EventReceiver{
    private Map<SenderType,List<String>> destinations;
    @Override
    public Map<SenderType, List<String>> getDestinations() {
        return this.destinations;
    }
}
