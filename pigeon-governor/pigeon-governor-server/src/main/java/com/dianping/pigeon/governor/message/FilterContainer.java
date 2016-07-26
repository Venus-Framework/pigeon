package com.dianping.pigeon.governor.message;

import java.util.List;

/**
 * Created by shihuashen on 16/7/19.
 */
public interface FilterContainer {
    List<EventFilter> getFilters(Event event);
    boolean registerFilter(EventFilter filter);
    boolean removeFilter(EventFilter filter);
}
