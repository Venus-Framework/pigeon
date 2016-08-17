package com.dianping.pigeon.governor.bean.Event;

import com.dianping.pigeon.governor.message.SendResult;
import com.dianping.pigeon.governor.util.GsonUtils;

import java.util.List;

/**
 * Created by shihuashen on 16/8/10.
 */
public class EventDetailBean extends EventBean{
    public SendResult getSendResultDetail() {
        return sendResultDetail;
    }

    public void setSendResultDetail(SendResult sendResultDetail) {
        this.sendResultDetail = sendResultDetail;
    }

    public void setSendResultDetail(String json){
        this.sendResultDetail =
                GsonUtils.fromJson(json,SendResult.class);
    }

    private SendResult sendResultDetail;
}
