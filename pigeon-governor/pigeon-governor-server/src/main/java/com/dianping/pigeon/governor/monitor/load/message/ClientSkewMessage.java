package com.dianping.pigeon.governor.monitor.load.message;

import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.util.GsonUtils;

import java.util.Date;
import java.util.Map;

/**
 * Created by shihuashen on 16/7/13.
 */
public class ClientSkewMessage implements Event{
    private String serverProjectName;
    private String clientProjectName;
    private Map<String,Long> flowDistributed;
    private Date createTime;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private String url;

    public String getServerProjectName() {
        return serverProjectName;
    }

    public void setServerProjectName(String serverProjectName) {
        this.serverProjectName = serverProjectName;
    }

    public String getClientProjectName() {
        return clientProjectName;
    }

    public void setClientProjectName(String clientProjectName) {
        this.clientProjectName = clientProjectName;
    }

    public Map<String, Long> getFlowDistributed() {
        return flowDistributed;
    }

    public void setFlowDistributed(Map<String, Long> flowDistributed) {
        this.flowDistributed = flowDistributed;
    }

    public void setCreatTime(){
        this.createTime = new Date();
    }

    @Override
    public String getSignature() {
        return "ClientSkew$"+clientProjectName+"$"+serverProjectName+"$"+this.getCreateTime().getTime();
    }

    @Override
    public String getTitle() {
        return "Pigeon客户端调用流量不均";
    }

    @Override
    public String getContent() {
        return GsonUtils.prettyPrint(GsonUtils.toJson(this,false),false);
    }

    @Override
    public Date getCreateTime() {
        return this.createTime;
    }

    @Override
    public String getSummary() {
        return "Pigeon Caller "+this.clientProjectName+"至Pigeon Server "+this.serverProjectName+"的调用量分布不均";
    }

    @Override
    public int getLevel() {
        return 1;
    }
}
