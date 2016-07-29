package com.dianping.pigeon.governor.monitor.load.message;

import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.util.GsonUtils;

import java.util.Date;
import java.util.Map;

/**
 * Created by shihuashen on 16/7/13.
 */
public class ServerSkewMessage implements Event{
    private String projectName;
    private Map<String,Long> flowDistributed;
    private String url;
    private Date createTime;
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Map<String, Long> getFlowDistributed() {
        return flowDistributed;
    }

    public void setFlowDistributed(Map<String, Long> flowDistributed) {
        this.flowDistributed = flowDistributed;
    }

    public void setCreateTime(){
        this.createTime = new Date();
    }
    @Override
    public String getSignature() {
        return "ServerSkew$"+projectName+"$"+this.getCreateTime().getTime();
    }

    @Override
    public String getTitle() {
        return "Pigeon服务端流量不均告警";
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
        return "Pigeon Server "+this.getProjectName()+" 的调用分布不均.";
    }

    @Override
    public int getLevel() {
        return 2;
    }
}
