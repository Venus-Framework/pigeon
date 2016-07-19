package com.dianping.pigeon.governor.bean.FlowMonitorBean.method;

import com.dianping.pigeon.governor.util.GsonUtils;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by shihuashen on 16/7/6.
 */
public class MethodDistributedGraphBean {
    class Data {
        private long value;
        private String name;
        public Data(String name,long value){
            this.name = name;
            this.value = value;
        }
    }
    private LinkedList<Data> list;
    public MethodDistributedGraphBean(){
        this.list = new LinkedList<Data>();
    }

    public MethodDistributedGraphBean(Map<String,Long> dataMap){
        this.list = new LinkedList<Data>();
        for(Iterator<String> iterator = dataMap.keySet().iterator();iterator.hasNext();){
            String name = iterator.next();
            list.add(new Data(name,dataMap.get(name)));
        }
    }
    public String getIpName(){
        LinkedHashSet<String> ipNames = new LinkedHashSet<String>();
        for(Iterator<Data> iterator = list.iterator();iterator.hasNext();){
            ipNames.add(iterator.next().name);
        }
        return GsonUtils.toJson(ipNames);
    }

    public String getData(){
        return GsonUtils.toJson(list);
    }
    public String toString(){
        return GsonUtils.toJson(this);
    }
    public LinkedList<Data> getList() {
        return list;
    }

    public void setList(LinkedList<Data> list) {
        this.list = list;
    }
}

