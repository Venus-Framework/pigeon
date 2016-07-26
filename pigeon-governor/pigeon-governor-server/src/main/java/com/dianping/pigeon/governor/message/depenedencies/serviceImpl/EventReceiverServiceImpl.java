package com.dianping.pigeon.governor.message.depenedencies.serviceImpl;

import com.dianping.pigeon.governor.dao.EventReceiverModelMapper;
import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.message.EventReceiver;
import com.dianping.pigeon.governor.message.depenedencies.bean.EventReceiverBean;
import com.dianping.pigeon.governor.message.depenedencies.dao.CustomEventReceiverMapper;
import com.dianping.pigeon.governor.message.depenedencies.service.EventReceiverService;
import com.dianping.pigeon.governor.model.EventReceiverModel;
import com.dianping.pigeon.governor.util.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by shihuashen on 16/7/21.
 */
@Service
public class EventReceiverServiceImpl implements EventReceiverService{
    @Autowired
    private CustomEventReceiverMapper customEventReceiverMapper;
    @Autowired
    private EventReceiverModelMapper eventReceiverModelMapper;
    @Override
    public EventReceiver getEventReceiver(Event event) {
        String signature  = event.getSignature();
        List<EventReceiverModel> models = customEventReceiverMapper.getReceiverWithEventSignature(signature);
        EventReceiver receiver = new EventReceiverBean(models);
        return receiver;
    }

    @Override
    public void setEventReceiver(Event event, EventReceiver receiver) {

    }


    @Override
    public void deleteEventReceiver(Event event, EventReceiver receiver) {

    }

    @Override
    public List<EventReceiverModel> getAllEventReceiver() {
        List<EventReceiverModel> models = customEventReceiverMapper.selectAllEvent();
        return models;
    }
}
