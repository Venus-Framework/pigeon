package com.dianping.pigeon.governor.message.depenedencies.bean;

import com.dianping.pigeon.governor.message.EventReceiver;
import com.dianping.pigeon.governor.message.SenderType;
import com.dianping.pigeon.governor.model.EventReceiverModel;

import java.util.*;

/**
 * Created by shihuashen on 16/7/21.
 */
public class EventReceiverBean implements EventReceiver{
    private List<EventReceiverModel> models;

    public EventReceiverBean(){
        this.models = new LinkedList<EventReceiverModel>();
    }
    public EventReceiverBean(List<EventReceiverModel> models){
        this.models = models;
    }

    public List<EventReceiverModel> getModels() {
        return models;
    }

    public void setModels(List<EventReceiverModel> models) {
        this.models = models;
    }

    @Override
    public Map<SenderType, List<String>> getDestinations() {
        Map<SenderType,List<String>> map = new HashMap<SenderType,List<String>>();
        for(Iterator<EventReceiverModel> iterator = this.models.iterator();
            iterator.hasNext();){
            EventReceiverModel model = iterator.next();
            SenderType senderType = SenderType.getSenderType(model.getRecevierType());
            List<String> addresses = new LinkedList<String>();
            String rawAddress = model.getAddress();
            String[] addressesArray = rawAddress.split(",");
            map.put(senderType,Arrays.asList(addressesArray));
        }
        return map;
    }
}
