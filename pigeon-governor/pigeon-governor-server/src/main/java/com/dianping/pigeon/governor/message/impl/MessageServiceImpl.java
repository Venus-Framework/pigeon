package com.dianping.pigeon.governor.message.impl;

import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.message.EventFilter;
import com.dianping.pigeon.governor.message.EventReceiver;
import com.dianping.pigeon.governor.message.MessageService;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Created by shihuashen on 16/7/15.
 */
public class MessageServiceImpl implements MessageService{
    private LinkedList<EventFilter> filters;
    @Override
    public void sendMessage(Event event) {
    }
    @Override
    public void registerFilter(EventFilter filter) {
        this.filters.add(filter);
    }




}
