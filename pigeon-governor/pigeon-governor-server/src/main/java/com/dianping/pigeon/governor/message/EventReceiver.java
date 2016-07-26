package com.dianping.pigeon.governor.message;

import java.util.List;
import java.util.Map;

/**
 * Created by shihuashen on 16/7/18.
 */
public interface EventReceiver {
    Map<SenderType,List<String>> getDestinations();
}
