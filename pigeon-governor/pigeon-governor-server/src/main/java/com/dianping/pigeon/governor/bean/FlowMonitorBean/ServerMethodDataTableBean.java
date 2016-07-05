package com.dianping.pigeon.governor.bean.FlowMonitorBean;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by shihuashen on 16/7/5.
 */
public class ServerMethodDataTableBean {
    private LinkedList<ServerMethodDataBean> list ;
    public ServerMethodDataTableBean(TransactionReport report){
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
}
