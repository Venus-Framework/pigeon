package com.dianping.pigeon.governor.bean.scanServiceDesc;

import com.dianping.pigeon.governor.model.ServiceHosts;
import com.google.gson.Gson;

import java.util.*;

/**
 * Created by shihuashen on 16/5/4.
 */
public class ScanStaticsBean {
    private List<ServiceHosts> serviceHostses;
    private Map<String,UpdateResultState> statics;
    private List<String> netFails =  new LinkedList<String>();
    private List<String> dbFails = new LinkedList<String>();
    private List<String> stables = new LinkedList<String>();
    private List<String> changeds = new LinkedList<String>();
    private List<String> creates = new LinkedList<String>();
    private List<String> replaces = new LinkedList<String>();
    public ScanStaticsBean(List<ServiceHosts> serviceHostsList,Map<String,UpdateResultState> staticsMap){
        this.serviceHostses = serviceHostsList;
        this.statics = staticsMap;
        Iterator<String> iterator = staticsMap.keySet().iterator();
        while(iterator.hasNext()){
            String serviceName = iterator.next();
            UpdateResultState updateResultState = statics.get(serviceName);
            switch(updateResultState){
                case STABLE:
                    stables.add(serviceName);
                    break;
                case CHANGED:
                    changeds.add(serviceName);
                    break;
                case DBFAIL:
                    dbFails.add(serviceName);
                    break;
                case CREATED:
                    creates.add(serviceName);
                    break;
                case NETFAIL:
                    netFails.add(serviceName);
                    break;
                case REPLACED:
                    replaces.add(serviceName);
                    break;
            }
        }
    }
    public int getServiceNum(){
        return this.serviceHostses.size();
    }
    public int getNetFailNum(){
        return netFails.size();
    }
    public int getDBFailNum(){
        return dbFails.size();
    }
    public int getCreateNum(){
        return creates.size();
    }
    public int getStableNum(){
        return  stables.size();
    }
    public int getChangedNum(){
        return changeds.size();
    }
    public int getReplaceNum(){
        return replaces.size();
    }

    public List<String> getNetFails(){
        return this.netFails;
    }
    public List<String> getDbFails(){
        return this.netFails;
    }
    public List<String> getStables(){
        return this.stables;
    }
    public List<String> getChangeds(){
        return this.changeds;
    }
    public List<String> getCreates(){
        return this.creates;
    }
    public List<String> getReplaces(){
        return this.replaces;
    }
}
