package com.dianping.pigeon.governor.message;

/**
 * Created by shihuashen on 16/7/21.
 */
public interface EventReceiverManager {
    EventReceiver getEventReceiver(Event event);
    boolean setEventReceiver(Event event,EventReceiver receiver);
    boolean deleteEventReceiver(Event event,EventReceiver receiver);
}
