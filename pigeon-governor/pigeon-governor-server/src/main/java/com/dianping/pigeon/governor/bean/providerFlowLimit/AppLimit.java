package com.dianping.pigeon.governor.bean.providerFlowLimit;

import com.dianping.pigeon.governor.exception.LionValuePraseErrorException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shihuashen on 16/9/9.
 */

public class AppLimit {
    Logger logger = LogManager.getLogger();
    private Map<String,Long> configs;
    /** raw String is app1:QPS,app2:QPS,...**/
    public AppLimit(String raw) throws LionValuePraseErrorException {
        this();
        String trimRaw = raw.trim();
        if(trimRaw.equals(""))
            return;
        if(trimRaw!=null){
            String[] strs = trimRaw.split(",");
            for(String s : strs){
                int splitIndex = s.indexOf(':');
                if(splitIndex<=0||splitIndex==s.length()-1)
                    throw new LionValuePraseErrorException();
                String projectName = s.substring(0,splitIndex);
                String rawValue = s.substring(splitIndex+1,s.length());
                try {
                    Long value = Long.valueOf(rawValue);
                    this.configs.put(projectName,value);
                }catch (NumberFormatException e){
                    logger.error(e);
                    throw new LionValuePraseErrorException();
                }
            }
        }
    }
    public AppLimit(){
        this.configs = new HashMap<String, Long>();
    }
    public boolean add(String appName,Long qpsLimitation){
        if(this.configs.containsKey(appName))
            return false;
        this.configs.put(appName,qpsLimitation);
        return true;
    }

    public boolean remove(String appName,Long qpsLimitation){
        if(this.configs.containsKey(appName)){
            this.configs.remove(appName);
            return true;
        }else return false;
    }

    public boolean update(String appName,Long qpsLimitation){
        if(this.configs.containsKey(appName)){
            this.configs.put(appName,qpsLimitation);
            return true;
        }else return false;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(this.configs.isEmpty())
            return "";
        for(String key : this.configs.keySet()){
            long value = this.configs.get(key);
            sb.append(key);
            sb.append(':');
            sb.append(value);
            sb.append(',');
        }
        return sb.substring(0,sb.length()-1);
    }
    public Map<String,Long> getConfigs(){
        return this.configs;
    }
}
