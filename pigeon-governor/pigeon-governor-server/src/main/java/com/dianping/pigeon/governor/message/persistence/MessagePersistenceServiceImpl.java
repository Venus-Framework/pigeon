package com.dianping.pigeon.governor.message.persistence;

import com.dianping.pigeon.governor.dao.EventModelMapper;
import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.message.SendResult;
import com.dianping.pigeon.governor.message.impl.SignatureUtils;
import com.dianping.pigeon.governor.model.EventModel;
import com.dianping.pigeon.governor.model.EventModelWithBLOBs;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.util.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by shihuashen on 16/7/27.
 */
@Service
public class MessagePersistenceServiceImpl implements MessagePersistenceService{
    @Autowired
    private EventModelMapper eventModelMapper;
    @Autowired
    private SignatureUtils signatureUtils;
    //TODO rebuild with cache
    @Override
    public void saveMessageSendReport(Event event, SendResult result) {
        EventModelWithBLOBs record = new EventModelWithBLOBs();
        EventModel model = signatureUtils.signatureAnalyze(event);
        record.setEventType(model.getEventType());
        record.setProjectId(model.getProjectId());
        record.setRelateProjectId(model.getRelateProjectId());
        record.setEventSignature(event.getSignature());
        record.setLevel(event.getLevel());
        record.setSummary(event.getSummary());
        record.setTitle(event.getTitle());
        record.setContent(event.getContent());
        record.setSendStatus(result.toString());
        eventModelMapper.insertSelective(record);
    }
//    @Autowired
//    private MessageLogModelMapper messageLogModelMapper;
//    @Override
//    public void saveMessageSendReport(Event event, SendResult result) {
//        MessageLogModelWithBLOBs record = new MessageLogModelWithBLOBs();
//        //TODO redefine
//        record.setEventSignature(event.getSignature());
//        record.setLevel(event.getLevel());
//        record.setSummary(event.getSummary());
//        record.setTitle(event.getTitle());
//        record.setContent(event.getContent());
//        //TODO define send status in sender.
//        record.setSendStatus(result.toString());
//        messageLogModelMapper.insertSelective(record);
//    }
}
