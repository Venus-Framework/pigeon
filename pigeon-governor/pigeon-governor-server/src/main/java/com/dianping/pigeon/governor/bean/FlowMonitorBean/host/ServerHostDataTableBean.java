package com.dianping.pigeon.governor.bean.FlowMonitorBean.host;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.pigeon.governor.bean.FlowMonitorBean.ServerMethodDataBean;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shihuashen on 16/7/7.
 */
public class ServerHostDataTableBean {
    private LinkedList<ServerHostDataBean> list ;
    private String projectName;
    private String time;
    private String ip;

    public ServerHostDataTableBean() {
        this.list = new LinkedList<ServerHostDataBean>();
    }


    public ServerHostDataTableBean(Collection<TransactionName> names, String projectName , String time, String ip){
        this.projectName = projectName;
        this.time = time;
        this.ip = ip;
        this.list = new LinkedList<ServerHostDataBean>();
        for(Iterator<TransactionName> iterator = names.iterator();iterator.hasNext();)
            this.list.add(new ServerHostDataBean(iterator.next()));
    }


    public LinkedList<ServerHostDataBean> getList() {
        return list;
    }

    public void setList(LinkedList<ServerHostDataBean> list) {
        this.list = list;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
