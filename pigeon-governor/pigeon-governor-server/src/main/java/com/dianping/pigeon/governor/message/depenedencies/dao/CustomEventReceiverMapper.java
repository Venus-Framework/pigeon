package com.dianping.pigeon.governor.message.depenedencies.dao;

import com.dianping.pigeon.governor.model.EventReceiverModel;

import java.util.List;

/**
 * Created by shihuashen on 16/7/21.
 */
public interface CustomEventReceiverMapper {
    List<EventReceiverModel> getReceiverWithEventSignature(String signature);
    List<EventReceiverModel> selectAllEvent();
}
