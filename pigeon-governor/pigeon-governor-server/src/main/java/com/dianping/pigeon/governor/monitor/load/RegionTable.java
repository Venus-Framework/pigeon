package com.dianping.pigeon.governor.monitor.load;

import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.util.IPUtils;

import java.util.*;

/**
 * Created by shihuashen on 16/7/28.
 */

//TODO refactor with trie tree
public class RegionTable {
    private volatile Map<String,List<String>> regions;
    public RegionTable (){
        this.regions = new HashMap<String, List<String>>();
    }

    public void loadRegionTable(){
        Map<String,List<String>> tmp = new HashMap<String,List<String>>();
//        String  rawData = Lion.get("pigeon.regions");
        String rawData = "beijing:10.64,10.65,10.32,10.33,10.4,10.5,10.12,10.13,10.16,10.17,10.8,10.10,10.128,10.96,10.97;shanghai:10.1,10.101,10.3,10.67,10.6,10.68,10.69";
        System.out.print(rawData);
        String[] rawRegionArray = rawData.split(";");
        for(int i = 0;i<rawRegionArray.length;i++){
            String rawRegion = rawRegionArray[i];
            int splitIndex = rawRegion.indexOf(':');
            String regionName = rawRegion.substring(0,splitIndex);
            String[] netPrefix = rawRegion.substring(splitIndex+1,rawRegion.length()).split(",");
            List<String> list = new LinkedList<String>();
            for (String s:
                 netPrefix)
                list.add(s);
            tmp.put(regionName,list);
        }
        this.regions = tmp;
    }
    //If the ipAddress belongs to one of the region, return the region name. Otherwise, return null.
    public String getRegionName(String ipAddress){
        for(Iterator<String> iterator = this.regions.keySet().iterator();
                iterator.hasNext();){
            String regionName = iterator.next();
            for(Iterator<String> iter = this.regions.get(regionName).iterator();
                    iter.hasNext();){
                String regionPrefix =iter.next();
                if(IPUtils.regionCheck(regionPrefix,ipAddress))
                    return regionName;
            }
        }
        return null;
    }
}
