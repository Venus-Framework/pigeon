package com.dianping.pigeon.governor.message.impl;

import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.message.EventReceiver;
import com.dianping.pigeon.governor.message.EventReceiverManager;
import com.dianping.pigeon.governor.message.SenderType;
import com.dianping.pigeon.governor.message.depenedencies.bean.EventReceiverBean;
import com.dianping.pigeon.governor.message.depenedencies.service.EventReceiverService;
import com.dianping.pigeon.governor.model.EventReceiverModel;
import com.dianping.pigeon.governor.util.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by shihuashen on 16/7/21.
 */
@Component
public class DefaultEventReceiverManager implements EventReceiverManager{
    private ModelCache modelCache;
    @Autowired
    private EventReceiverService eventReceiverService;

    @Override
    public void init(){
        this.modelCache = new ModelCache(5,3);
        System.out.println(this.eventReceiverService);
        this.modelCache.setEventReceiverService(this.eventReceiverService);
        this.modelCache.schedule();
    }

    @Override
    public EventReceiver getEventReceiver(Event event) {
        List<EventReceiverModel> models = this.modelCache.getCache().get("1");
        if(models!=null)
            return new EventReceiverBean(models);
        else{
            //TODO project cache.
            return null;
        }
    }

    @Override
    public boolean setEventReceiver(Event event, EventReceiver receiver) {
        return false;
    }

    @Override
    public boolean deleteEventReceiver(Event event, EventReceiver receiver) {
        return false;
    }


    class ModelCache implements Runnable{
        private volatile ConcurrentHashMap<String,List<EventReceiverModel>> cache;
        private ScheduledExecutorService service;
        private int delayTime;

        public void setEventReceiverService(EventReceiverService eventReceiverService) {
            this.eventReceiverService = eventReceiverService;
        }

        private EventReceiverService eventReceiverService;
        public ModelCache(int poolSize,int delayTime){
            this.cache = new ConcurrentHashMap<String, List<EventReceiverModel>>();
            this.service = Executors.newScheduledThreadPool(poolSize);
            this.delayTime = delayTime;
        }

        public void schedule(){
            service.scheduleAtFixedRate(
                    this,0,
                    delayTime, TimeUnit.MINUTES);
        }
        public ConcurrentHashMap<String,List<EventReceiverModel>> getCache(){
            return this.cache;
        }

        @Override
        public void run() {
            List<EventReceiverModel> models = eventReceiverService.getAllEventReceiver();
            ConcurrentHashMap<String,List<EventReceiverModel>> tmp = new ConcurrentHashMap<String,List<EventReceiverModel>>();
            for(Iterator<EventReceiverModel> iterator = models.iterator();iterator.hasNext();){
                EventReceiverModel model = iterator.next();
                //TODO rejudge of the signature.
                String key = (model.getEventSignature());
                if(tmp.containsKey(key))
                    tmp.get(key).add(model);
                else{
                    List<EventReceiverModel> list = new LinkedList<EventReceiverModel>();
                    list.add(model);
                    tmp.put(key,list);
                }
            }
            cache = tmp;
        }
    }
}
