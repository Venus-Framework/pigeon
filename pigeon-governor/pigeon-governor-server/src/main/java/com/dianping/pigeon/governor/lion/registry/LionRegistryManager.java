package com.dianping.pigeon.governor.lion.registry;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.config.RegistryConfigManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;

public class LionRegistryManager {

	private static final Logger logger = LogManager.getLogger();

	private Properties props = new Properties();

	private static volatile boolean isInit = false;

	private static Throwable initializeException = null;

	private static LionRegistryManager instance = new LionRegistryManager();

	private static RegistryConfigManager registryConfigManager = new LionRegistryConfigManager();

	//创建一个新的实例，用来指向lion的zk集群
	private static Registry registry = new LionCuratorRegistry();

	private LionRegistryManager() {
	}

	public static boolean isInitialized() {
		return isInit;
	}

	public static Throwable getInitializeException() {
		return initializeException;
	}

	public static LionRegistryManager getInstance() {
		if (!isInit) {
			synchronized (LionRegistryManager.class) {
				if (!isInit) {
					instance.init(registryConfigManager.getRegistryConfig());
					initializeException = null;
					isInit = true;
				}
			}
		}
		return instance;
	}

	private void init(Properties properties) {
		instance.setProperties(properties);
		String registryType = properties.getProperty(Constants.KEY_REGISTRY_TYPE);
		if (!Constants.REGISTRY_TYPE_LOCAL.equalsIgnoreCase(registryType)) {
			if (registry != null) {
				try {
					logger.info("初始化lionRegistry");
					registry.init(properties);
				} catch (Throwable t) {
					initializeException = t;
					throw new RuntimeException(t);
				}
			}
		} else {
		}
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setProperty(String key, String value) {
		// 如果是dev环境，可以把当前配置加载进去
		props.put(key, value);
	}

	public void setProperties(Properties props) {
		this.props.putAll(props);
	}

	
	public void setServerWeight(String serverAddress, int weight) throws RegistryException {
		if (registry != null) {
			registry.setServerWeight(serverAddress, weight);
			logger.info("PigeonService.weight:" + weight);
		}
	}
	
	/**
	 * 
	 * @author chenchongze
	 * @param serviceName
	 * @param group
	 * @param hosts
	 */
	public void setServerService(String serviceName, String group, String hosts) throws RegistryException {
		if( registry != null) {
			registry.setServerService(serviceName, group, hosts);
			logger.info("PigeonService.setHosts:" + serviceName + ",swimlane=" + group + "&hosts=" + hosts);
		}
	}
	
	public void delServerService(String serviceName, String group) throws RegistryException {
		if( registry != null) {
			registry.delServerService(serviceName, group);
			logger.info("PigeonService.delService:" + serviceName + ",swimlane=" + group);
		}
	}
}
