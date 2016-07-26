package com.dianping.pigeon.governor.bean.flowMonitor;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.pigeon.governor.util.CatReportXMLUtils;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by shihuashen on 16/7/5.
 */
public class ServerMethodDataTableBean {
    private LinkedList<ServerMethodDataBean> list ;
    private String projectName;
    private String time;
    private String ip;
    public ServerMethodDataTableBean(TransactionReport report){
        projectName = report.getDomain();
        time = CatReportXMLUtils.dateFormat(report.getStartTime());
        {
            Iterator<String> iterator = report.getIps().iterator();
            ip = iterator.next();
        }
        list = new LinkedList<ServerMethodDataBean>();
        for(Iterator<TransactionName> iterator = report.getMachines().get("All").getTypes().get("PigeonService").getNames().values().iterator();
                iterator.hasNext();){
            list.add(new ServerMethodDataBean(iterator.next()));
        }
    }

    public LinkedList<ServerMethodDataBean> getList() {
        return list;
    }

    public void setList(LinkedList<ServerMethodDataBean> list) {
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
