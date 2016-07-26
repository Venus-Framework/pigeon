package com.dianping.pigeon.governor.bean.flowMonitor;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.google.gson.Gson;
import java.util.*;
/**
 * Created by shihuashen on 16/7/4.
 */
public class ServerSummaryGraphBean {
    private Set<String> elementName;
    private Map<String,Long> serviceMap;
    private Map<String,Long> methodMap;
    public ServerSummaryGraphBean(TransactionReport report){
        elementName = new LinkedHashSet<String>();
        serviceMap = new LinkedHashMap<String,Long>();
        methodMap = new LinkedHashMap<String, Long>();
        TransactionType pigeonService = report.getMachines().get("All").getTypes().get("PigeonService");
        Map<String, TransactionName> nameMap = pigeonService.getNames();
        fillData(nameMap);
    }
    private void fillData(Map<String, TransactionName> nameMap) {
        String prefix = "";
        long sum = 0;
        Map<String,Long> map = new LinkedHashMap<String,Long>();
        Collection<String> keySet= nameMap.keySet();
        List<String> list = new LinkedList<String>(keySet);
        Collections.sort(list);
        for(Iterator<String> iterator = list.iterator();iterator.hasNext();){
            String name = iterator.next();
            long count = nameMap.get(name).getTotalCount();
            methodMap.put(name.substring(name.lastIndexOf(":")+1,name.length()),count);
            String serviceName = name.substring(0,name.lastIndexOf(":"));
            if(serviceName.equals(prefix))
                sum+=count;
            else{
                if(!prefix.equals("")){
                    serviceMap.put(prefix,sum);
                }
                prefix = serviceName;
                sum = count;
            }
        }
        if(!prefix.equals(""))
            serviceMap.put(prefix,sum);
        elementName.addAll(serviceMap.keySet());
        elementName.addAll(methodMap.keySet());
    }
    public String getElemntName(){
        return new Gson().toJson(this.elementName);
    }
    public String getServiceMap(){
        Set<data> set = new LinkedHashSet<data>();
        for(Iterator<String> iterator = serviceMap.keySet().iterator();iterator.hasNext();){
            String name = iterator.next();
            data data = new data(name,serviceMap.get(name));
            set.add(data);
        }
        return new Gson().toJson(set);
    }
    public String getMethodMap(){
        Set<data> set = new LinkedHashSet<data>();
        for(Iterator<String> iterator = methodMap.keySet().iterator();iterator.hasNext();){
            String name = iterator.next();
            data data = new data(name,methodMap.get(name));
            set.add(data);
        }
        return new Gson().toJson(set);
    }
    class data{
        private long value;
        private String name;
        public data(String name,long value){
            this.value = value;
            this.name = name;
        }
    }

}
