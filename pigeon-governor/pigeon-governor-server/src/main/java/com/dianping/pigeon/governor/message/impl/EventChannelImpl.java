package com.dianping.pigeon.governor.message.impl;

import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.message.EventChannel;
import com.dianping.pigeon.governor.util.GsonUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by shihuashen on 16/7/15.
 */
@Component
public class EventChannelImpl implements EventChannel {
    private BlockingQueue<Event> eventBuffer;
    private int bufferSize = 3000;

    public EventChannelImpl() {
        eventBuffer = new ArrayBlockingQueue<Event>(bufferSize);
    }

    public EventChannelImpl(int bufferSize) {
        this.bufferSize = bufferSize;
        eventBuffer = new ArrayBlockingQueue<Event>(bufferSize);
    }

    @Override
    public void put(Event event) throws InterruptedException {
        eventBuffer.put(event);
    }

    @Override
    public Event take() throws InterruptedException {
        return eventBuffer.take();
    }
}
