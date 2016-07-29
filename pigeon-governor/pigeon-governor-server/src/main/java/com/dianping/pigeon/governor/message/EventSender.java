package com.dianping.pigeon.governor.message;

/**
 * Created by shihuashen on 16/7/19.
 */
public interface EventSender {
    SendResult sendMessage(Event event,EventReceiver receiver);
}
