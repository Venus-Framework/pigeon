package com.dianping.pigeon.config.group;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/5/11.
 */
public enum GroupManager {

    INSTANCE;
    private GroupManager() {
        //TODO 加载动态变更config listener
        configManager.registerConfigChangeListener(new InnerConfigChangeListener());
    }

    final private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private Logger logger = LoggerLoader.getLogger(this.getClass());

    private final String localIp = configManager.getLocalIp();
    private final String envGroup = configManager.getGroup();

    private final static String GROUP_INVOKER_BASE = "pigeon.group.invoker.";
    private final static String GROUP_PROVIDER_BASE = "pigeon.group.provider.";
    private final static int GROUP_INVOKER_BASE_LENGTH = GROUP_INVOKER_BASE.length();
    private final static int GROUP_PROVIDER_BASE_LENGTH = GROUP_PROVIDER_BASE.length();

    private Map<String, String> invokerGroupCache = new ConcurrentHashMap<String, String>();

    public String getInvokerGroup(String serviceName) {
        String group = invokerGroupCache.get(serviceName);

        if(group == null) {
            String groupConfigs = configManager.getStringValue(GROUP_INVOKER_BASE + Utils.escapeServiceName(serviceName));

            if(StringUtils.isNotBlank(groupConfigs)) {
                group = parseLocalGroup(groupConfigs);
            } else {
                group = envGroup;
            }

            invokerGroupCache.put(serviceName, group);
        }

        return group;
    }

    public String getProviderGroup(String serviceName) {
        String groupConfigs = configManager.getStringValue(GROUP_PROVIDER_BASE + Utils.escapeServiceName(serviceName));
        if(StringUtils.isBlank(groupConfigs)) {
            return parseLocalGroup(groupConfigs);
        }
        return envGroup;
    }

    private String parseLocalGroup(String groupConfigs) {
        try {
            String[] keyVals = groupConfigs.split(",");
            for(String keyVal : keyVals) {
                String[] ipGroupArray = keyVal.split(":");
                String ip = ipGroupArray[0];
                if(localIp.equals(ip)) {
                    return ipGroupArray[1];
                }
            }
            return envGroup;
        } catch (Throwable t) {
            logger.warn("Parse group config error! return appenv group: " + envGroup, t);
            return envGroup;
        }
    }

    private class InnerConfigChangeListener implements ConfigChangeListener {

        @Override
        public void onKeyUpdated(String key, String value) {
            int _index = key.indexOf(GROUP_INVOKER_BASE);
            if(_index != -1) {
                //TODO invoker改变分组设置时，通知变化，重新连接新分组和断开旧分组
                String _serviceName = Utils.unescapeServiceName(key.substring(_index + GROUP_INVOKER_BASE_LENGTH));
                //TODO 比对serviceName，找到invoker，修改invokerGroupCache和invokerConfig配置
                for(String serviceName : invokerGroupCache.keySet()) {
                    if(serviceName.equals(_serviceName)) {
                        String group = envGroup;

                        if(StringUtils.isNotBlank(value)) {
                            group = parseLocalGroup(value);
                        }

                        invokerGroupCache.put(serviceName, group);
                        // TODO invokerConfig修改

                    }
                }

                return;
            }

            _index = key.indexOf(GROUP_PROVIDER_BASE);
            if(_index != -1) {

            }


            /*if(key.contains(GROUP_INVOKER_BASE)) {
            } else if(key.contains(GROUP_PROVIDER_BASE)) {
            }*/
        }

        @Override
        public void onKeyAdded(String key, String value) {

        }

        @Override
        public void onKeyRemoved(String key) {

        }
    }
}
