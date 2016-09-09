package com.dianping.pigeon.governor.util;

import com.dianping.pigeon.governor.bean.config.RouterInfo;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Created by shihuashen on 16/5/19.
 */

//TODO we need a better understand of the Lion http client response.
public class LionUtils {
    private static Logger logger = LogManager.getLogger();
    public static GetPrefixLionHttpResponse getLionConfigs(String prefix, String env){
        String url = "http://lionapi.dp:8080/config2/get?env="+env+"&id=2&prefix="+prefix;
        System.out.println(url);
        String response  = null;
        response = HttpCallUtils.httpGet(url);
        System.out.println(response);
        Gson gson = new Gson();
        if(response!= null)
            return gson.fromJson(response,GetPrefixLionHttpResponse.class);
        return null;
    }
    public class GetPrefixLionHttpResponse {
        private String status;
        private String message;
        private Map<String,String> result;
        public Map<String,String> getResult(){
            return result;
        }
    }
    public class GetKeyLionResponse {
        private String status;
        private String message;
        private String result;
        public String getResult(){
            return result;
        }
    }
    public class GetProjectLionResponse{
        private String status;
        private String message;
        private List<String> result;
        public List<String> getResult(){
            return result;
        }
    }
    public class SetConfigLionResponse{
        private String status;
        private String message;
        private String result;
        public String getStatus(){
            return this.status;
        }
    }
    
    
    //TODO parsing the json result. Check whether the create success or not.
    public static void createConfig(String env,String projectName,String key,String desc){
        String url = "http://lionapi.dp:8080/config2/create?env="+env+"&id=2&project="+projectName+"&key="+key+
                "&desc="+desc;
        System.out.println(url);
        HttpCallUtils.httpGet(url);
    }

    //TODO parsing the json result. Check whether the set success or not;
    public static boolean setConfig(String env,String lionKey,String lionValue){
        String url = "http://lionapi.dp:8080/config2/set?env="+env+"&id=2&key="+lionKey+"&value="+lionValue;
        System.out.println(url);
        String response = HttpCallUtils.httpGet(url);
        if(response!=null){
            SetConfigLionResponse  setConfigLionResponse = GsonUtils.fromJson(response,SetConfigLionResponse.class);
            if(setConfigLionResponse.getStatus().equals("success"))
                return true;
        }
        return false;
    }

    public static boolean isExistKey(String env, String lionKey){
        String url = "http://lionapi.dp:8080/config2/get?env="+env+"&id=2&key="+lionKey;
        String response = null;
        response = HttpCallUtils.httpGet(url);
        if(response!=null){
            System.out.println(response);
            GetKeyLionResponse getKeyLionResponse = new Gson().fromJson(response,GetKeyLionResponse.class);
            if(getKeyLionResponse.getResult()!=null)
                return true;
            else
                return false;
        }
        return false;
    }


    public static String getKey(String env,String lionKey){
        String url = "http://lionapi.dp:8080/config2/get?env="+env+"&id=2&key="+lionKey;
        String response = null;
        response = HttpCallUtils.httpGet(url);
        if(response!=null){
            GetKeyLionResponse getKeyLionResponse = new Gson().fromJson(response,GetKeyLionResponse.class);
            return getKeyLionResponse.getResult();
        }
        return null;
    }

    public static boolean isExistProject(String projectName){
        String url = "http://lionapi.dp:8080/config2/list?project="+projectName;
        String response = null;
        response = HttpCallUtils.httpGet(url);
        if(response!=null){
            try{
                GetProjectLionResponse getPrefixLionHttpResponse = new Gson().fromJson(response,GetProjectLionResponse.class);
            }catch (Throwable t){
                logger.error(t);
                t.printStackTrace();
                return false;
            }
            return true;
        }else
            return false;
    }
    public static Map<String,List<RouterInfo>> getServiceRouterConfigsPerProject(String projectName,String type,String env){
        GetPrefixLionHttpResponse response = getLionConfigs(projectName+".pigeon.group."+type,env);
        System.out.println(new Gson().toJson(response));
        return constructServiceRouterConfigs(response,type);
    }

    private static Map<String,List<RouterInfo>> constructServiceRouterConfigs(GetPrefixLionHttpResponse response, String type){
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
