package com.dianping.pigeon.governor.util;

import com.dianping.pigeon.governor.bean.ConfigBean.RouterInfo;
import com.dianping.pigeon.governor.bean.ConfigBean.ServiceConfigBean;
import com.google.gson.Gson;

import java.util.*;

/**
 * Created by shihuashen on 16/5/19.
 */
public class LionUtils {

    public static LionHttpResponse getLionConfigs(String prefix,String env){
        String url = "http://lionapi.dp:8080/config2/get?env="+env+"&id=2&prefix="+prefix;
        System.out.println(url);
        String response  = null;
        response = HttpCallUtils.httpGet(url);
        System.out.println(response);
        Gson gson = new Gson();
        if(response!= null)
            return gson.fromJson(response,LionHttpResponse.class);
        return null;
    }
    public class LionHttpResponse{
        private String status;
        private String message;
        private Map<String,String> result;
        public Map<String,String> getResult(){
            return result;
        }
    }

    public static Map<String,List<RouterInfo>> getServiceRouterConfigsPerProject(String projectName,String type,String env){
        LionHttpResponse response = getLionConfigs(projectName+".pigeon.group."+type,env);
        System.out.println(new Gson().toJson(response));
        return constructServiceRouterConfigs(response,type);
    }

    private static Map<String,List<RouterInfo>> constructServiceRouterConfigs(LionHttpResponse response,String type){
        HashMap<String,List<RouterInfo>> map = new HashMap<String,List<RouterInfo>>();
        Map<String,String> configs = response.getResult();
        Iterator<String> iterator = configs.keySet().iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            int start = key.indexOf(type)+type.length()+1;
            String ipAddress = key.substring(start,key.length());
            String[] pairs = configs.get(key).split(",");
            for(int i = 0 ; i <pairs.length;i++){
                int splitIndex = findRouterInfoSplitIndex(pairs[i]);
                if(splitIndex!=-1){
                    String serviceName = pairs[i].substring(0,splitIndex);
                    String group = pairs[i].substring(splitIndex+1,pairs[i].length());
                    RouterInfo routerInfo  =  new RouterInfo(ipAddress,group);
                    if(map.containsKey(serviceName)){
                        map.get(serviceName).add(routerInfo);
                    }else{
                        LinkedList<RouterInfo> routerInfos = new LinkedList<RouterInfo>();
                        routerInfos.add(routerInfo);
                        map.put(serviceName,routerInfos);
                    }
                }
            }
        }
        return map;
    }

    private static int findRouterInfoSplitIndex(String s){
        for(int i =s.length()-1;i>-1;i--){
            if(s.charAt(i)==':')
                return i;
        }
        return -1;
    }
    public static Map<String,String> convertRawLionConfigValue(String s){
        Map<String,String> map = new HashMap<String,String>();
        String[] keyValues = s.split(",");
        for(int i = 0;i<keyValues.length;i++){
            int splitIndex = findRouterInfoSplitIndex(keyValues[i]);
            if(splitIndex!=-1){
                String key = keyValues[i].substring(0,splitIndex);
                String value = keyValues[i].substring(splitIndex+1,keyValues[i].length());
                map.put(key,value);
            }
        }
        return map;
    }
    public static String convertMapToRawLionConfigValue(Map<String,String> map){
        Iterator<String> iterator = map.keySet().iterator();
        int size = map.size();
        if(size==0)
            return "";
        if(size==1){
            String key = iterator.next();
            String value = map.get(key);
            return key+":"+value;
        }
        String s = "";
        while(iterator.hasNext()){
            String key = iterator.next();
            String value = map.get(key);
            s+=(key+":"+value+",");
        }
        s =  s.substring(0,s.length()-1);
        return s;
    }
}
