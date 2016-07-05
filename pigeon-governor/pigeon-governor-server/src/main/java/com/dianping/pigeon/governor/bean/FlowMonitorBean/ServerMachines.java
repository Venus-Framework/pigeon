package com.dianping.pigeon.governor.bean.FlowMonitorBean;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.pigeon.governor.util.GsonUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by shihuashen on 16/7/5.
 */
public class ServerMachines {
    private LinkedList<ServerMachine> machines;
    public ServerMachines(TransactionReport report){
        machines = new LinkedList<ServerMachine>();
        for(Iterator<String> iterator = report.getIps().iterator();iterator.hasNext();){
            String i = iterator.next();
            machines.add(new ServerMachine(i));
        }
        System.out.println(GsonUtils.prettyPrint(GsonUtils.toJson(this)));
    }

    public LinkedList<ServerMachine> getMachines() {
        return machines;
    }

    public void setMachines(LinkedList<ServerMachine> machines) {
        this.machines = machines;
    }
}
