package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.Event.EventBean;
import com.dianping.pigeon.governor.bean.Event.EventDetailBean;
import com.dianping.pigeon.governor.bean.Event.FilterBean;

import java.util.List;

/**
 * Created by shihuashen on 16/8/5.
 */
public interface EventService {
    List<EventBean> getRecentEvents(int size);
    List<EventBean> filterEvents(FilterBean filterBean);
    int getTotalCount();
//    EventDetailBean getEventDetail(int eventId);
}
