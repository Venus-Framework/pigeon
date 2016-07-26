package com.dianping.pigeon.governor.message.depenedencies.service;

import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.message.EventReceiver;
import com.dianping.pigeon.governor.model.EventReceiverModel;

import java.util.List;

/**
 * Created by shihuashen on 16/7/21.
 */
public interface EventReceiverService {
    EventReceiver getEventReceiver(Event event);
    void setEventReceiver(Event event,EventReceiver receiver);
    void deleteEventReceiver(Event event,EventReceiver receiver);
    List<EventReceiverModel> getAllEventReceiver();
}
