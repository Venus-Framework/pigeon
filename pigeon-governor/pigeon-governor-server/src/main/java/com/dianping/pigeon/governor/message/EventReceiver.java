package com.dianping.pigeon.governor.message;

import java.util.List;

/**
 * Created by shihuashen on 16/7/18.
 */
public interface EventReceiver {
    List<String> obtainDestinations(Event event);
}
