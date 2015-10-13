package com.dianping.pigeon.governor.lion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;

import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.Lion;

public class ConfigHolder {
	
	private static boolean init = false;
	
	private static Logger log = LogManager.getLogger();
	
	private final static Map<String , String> lionConfigMap = new ConcurrentHashMap<String, String>();
	
	public static String get(LionKeys keyEnum){
		return get(keyEnum.value(),"");
	}
	
	public static String get(String key){
		return get(key,"");
	}
	
	public static String get(String key, String defaultValue){
		String result = null;
		
		try {
			result = lionConfigMap.get(key);
		} catch (Exception e) {
			log.info("Read Lion Key Error, read from defaultValue: " + defaultValue);
			result = defaultValue;
		}
		
		return result;
	}
	
	public void init(){
		
		if(!init){
			synchronized(ConfigHolder.class){
				
				if(!init){
					log.info("ConfigHolder initialize ...");
					
					for(LionKeys key : LionKeys.values()){
						
						String lionValue = Lion.get(key.value());
						
						if(StringUtils.isBlank(lionValue)) {
							lionValue = "";
						}
						
						lionConfigMap.put(key.value(), lionValue);
					}
					
					Lion.addConfigChangeListener(new ConfigChange(){

						@Override
						public void onChange(String key, String value) {
							
							lionConfigMap.put(key, value);
							
						}
						
					});
					
					init = true;
				}
				
			}
			
		}
	}
	
	
}
