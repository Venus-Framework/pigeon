package com.dianping.pigeon.governor.bean.flowMonitor;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.pigeon.governor.util.CatReportXMLUtils;
import com.dianping.pigeon.governor.util.GsonUtils;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by shihuashen on 16/7/5.
 */
public class ServerMachines {
    private String projectName;
    private String date;
    private LinkedList<ServerMachine> machines;
    public ServerMachines(TransactionReport report){
        this.projectName = report.getDomain();
        this.date = CatReportXMLUtils.dateFormat(report.getStartTime());
        machines = new LinkedList<ServerMachine>();
        for(Iterator<String> iterator = report.getIps().iterator();iterator.hasNext();){
            String i = iterator.next();
            machines.add(new ServerMachine(projectName,date,i));
        }
        System.out.println(GsonUtils.prettyPrint(GsonUtils.toJson(this)));
    }

    public LinkedList<ServerMachine> getMachines() {
        return machines;
    }

    public void setMachines(LinkedList<ServerMachine> machines) {
        this.machines = machines;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
