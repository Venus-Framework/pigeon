package com.dianping.pigeon.governor.message.persistence;

import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.message.SendResult;

/**
 * Created by shihuashen on 16/7/27.
 */
public interface MessagePersistenceService {
    void saveMessageSendReport(Event event, SendResult result);
}
