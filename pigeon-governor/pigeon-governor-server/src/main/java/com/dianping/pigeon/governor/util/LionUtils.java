package com.dianping.pigeon.governor.util;

import com.dianping.pigeon.governor.bean.ConfigBean.RouterInfo;
import com.dianping.pigeon.governor.bean.ConfigBean.ServiceConfigBean;

import java.util.HashMap;

/**
 * Created by shihuashen on 16/5/19.
 */
public class LionUtils {
    public static RouterInfo constructRouterInfo(String str){
        //TODO
        int index = str.indexOf(':');
        String ipAddress = str.substring(0,index);
        String group = str.substring(index+1,str.length()-1);
        RouterInfo routerInfo = new RouterInfo();
        routerInfo.setIpAddress(ipAddress);
        routerInfo.setGroup(group);
        return routerInfo;
    }

    public static ServiceConfigBean constructServiceConfig(String str1,String str2){
        ServiceConfigBean serviceConfigBean = new ServiceConfigBean();
        String[] str1s = str1.split(",");
        HashMap<String,RouterInfo> map = new HashMap<String,RouterInfo>();
        for (String s:
             str1s) {
            map.put(s,constructRouterInfo(s));
        }
        serviceConfigBean.setInvokerConfigs(map);
        String[] str2s = str2.split(",");
        HashMap<String,RouterInfo> map2 = new HashMap<String,RouterInfo>();
        for(String s:
                str2s){
            map2.put(s,constructRouterInfo(s));
        }
        serviceConfigBean.setProviderConfigs(map2);
        return serviceConfigBean;
    }
}
