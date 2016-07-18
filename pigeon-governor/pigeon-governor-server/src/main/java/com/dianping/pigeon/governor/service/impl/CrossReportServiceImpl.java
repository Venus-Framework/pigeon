package com.dianping.pigeon.governor.service.impl;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.service.CrossReportService;
import com.dianping.pigeon.governor.util.CatReportXMLUtils;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.HttpCallUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by shihuashen on 16/7/11.
 */
@Service
public class CrossReportServiceImpl implements CrossReportService{
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    @Override
    public CrossReport getCrossReport(String projectName, String dateTime, String ip) {
        String url = getCatAddress()+"cat/r/cross?domain="+projectName+"&ip="+ip+"date="+dateTime+"&forceDownload=xml";
        System.out.println(url);
        String xml = HttpCallUtils.httpGet(url);
        System.out.println(xml);
        return CatReportXMLUtils.XMLToCrossReport(xml);
    }



    private String getCatAddress(){
        String env = configManager.getEnv();
        if(env.equals("qa"))
            return Constants.qaCatAddress;
        if(env.equals("prelease"))
            return Constants.ppeCatAddress;
        return Constants.onlineCatAddress;
    }


    //扫描CrossReport,确认服务端收到的调用请求和客户端汇总的调用请求数是否一致.
    public void serverAndClientCountCheck(CrossReport report){
        Map<String,Local> locals = report.getLocals();
        for(Iterator<String> iterator = locals.keySet().iterator();iterator.hasNext();){
            Local local = locals.get(iterator.next());
            Map<String,Remote> remotes = local.getRemotes();
            Map<String,Long> tmpMap = new HashMap<String,Long>();
            for(Iterator<String> iter = remotes.keySet().iterator();iter.hasNext();){
                Remote remote = remotes.get(iter.next());
                String role = remote.getRole();
                String ip = remote.getIp();
                System.out.println("role:"+role);
                System.out.println("ip:"+ip);
                if(role.equals("Pigeon.Caller")||role.equals("Pigeon.Client")){
                    if(tmpMap.containsKey(ip)){
                        long count =  tmpMap.get(ip);
                        if(!serverClientCountCompare(remote.getType().getTotalCount(),count)){
                            System.out.println(remote.getApp()+":"+ip+" count1 as :"+remote.getType().getTotalCount()
                            +" count2 as :"+count);
                            tmpMap.remove(ip);
                        }
                    }else{
                        tmpMap.put(ip,remote.getType().getTotalCount());
                    }
                }
            }
        }
    }
    //具体对比的方法.如果Server和Client的数据相差不大,那么可以认为没有异常.关于对比方法应该是可配置的.
    private boolean serverClientCountCompare(long count1,long count2){
        //默认机制,是否相等
        if(count1==count2)
            return true;
        else
            return false;
    }
}
