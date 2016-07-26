package com.dianping.pigeon.governor.message.impl;

import com.dianping.pigeon.governor.message.*;
import com.dianping.pigeon.governor.util.GsonUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Created by shihuashen on 16/7/15.
 */
public class MessageServiceImpl implements MessageService{
    private FilterContainer filterContainer;
    private EventChannel channel;
    private ChannelHandler channelHandler;
    private DefaultEventReceiverManager eventReceiverManager;
    private EventSender sender;

    public MessageServiceImpl(){
        this.filterContainer = new DefaultFilterContainer();
        this.channel = new EventChannelImpl();
        this.eventReceiverManager = new DefaultEventReceiverManager();
        this.sender = new PaasSender();
        this.channelHandler = new ChannelHandler();
    }
    public void init() throws Exception {
        this.eventReceiverManager.init();
        channelHandler.init(20,this.channel,this);
        start();
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
                sender.sendMessage(event,receiver);
                //TODO persistent the send result and log.
            }
        };
    }
}
