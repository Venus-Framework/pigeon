package com.dianping.pigeon.governor.message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by shihuashen on 16/7/15.
 */
public interface EventChannel {
    void put(Event e) throws InterruptedException;
    Event take() throws InterruptedException;
}
