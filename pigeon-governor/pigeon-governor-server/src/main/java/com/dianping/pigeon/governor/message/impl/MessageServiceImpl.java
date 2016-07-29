package com.dianping.pigeon.governor.message.impl;

import com.dianping.pigeon.governor.message.*;
import com.dianping.pigeon.governor.message.persistence.MessagePersistenceService;
import com.dianping.pigeon.governor.util.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

/**
 * Created by shihuashen on 16/7/15.
 */
@Service
public class MessageServiceImpl implements MessageService{
    @Autowired
    private FilterContainer filterContainer;
    @Autowired
    private EventChannel channel;
    @Autowired
    private ChannelHandler channelHandler;
    @Autowired
    private EventReceiverManager eventReceiverManager;
    @Autowired
    private EventSender sender;
    @Autowired
    private MessagePersistenceService messagePersistenceService;
    private volatile boolean isInited = false;
    public void init() throws Exception {
        if(!isInited){
            this.eventReceiverManager.init();
            channelHandler.init(20,this.channel,this);
            start();
            isInited = true;
        }
    }
    @Override
    public void sendMessage(Event event) {
        //TODO delay if blocking
        try {
            channel.put(event);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void registerFilter(EventFilter filter) {
        filterContainer.registerFilter(filter);
    }


    private void start() throws Exception {
        this.channelHandler.doStart();
    }
    public Runnable getEventLifeCycle(final Event event){
        return new Runnable() {
            @Override
            public void run() {
                List<EventFilter> filters = filterContainer.getFilters(event);
                for(Iterator<EventFilter> iterator = filters.iterator();iterator.hasNext();){
                    EventFilter filter = iterator.next();
                    if(!filter.doFilter(event)){
                        //TODO filtered! Ignore this message.
                        return;
                    }
                }
                EventReceiver receiver = eventReceiverManager.getEventReceiver(event);
                SendResult result = sender.sendMessage(event,receiver);
                messagePersistenceService.saveMessageSendReport(event,result);
                //TODO persistent the send result and log.
            }
        };
    }
}
