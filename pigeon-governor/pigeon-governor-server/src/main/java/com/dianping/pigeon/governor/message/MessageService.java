package com.dianping.pigeon.governor.message;

/**
 * Created by shihuashen on 16/7/15.
 */
public interface MessageService {
    void sendMessage(Event event);
    void registerFilter(EventFilter filter);
}
