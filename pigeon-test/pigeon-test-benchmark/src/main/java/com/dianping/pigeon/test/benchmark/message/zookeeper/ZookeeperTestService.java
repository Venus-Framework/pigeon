package com.dianping.pigeon.test.benchmark.message.zookeeper;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.pigeon.test.benchmark.message.AbstractMessageTestService;
import com.dianping.pigeon.test.benchmark.message.MessageTestService;

@Service(url = "com.dianping.message.test.ZookeeperTestService", interfaceClass = MessageTestService.class)
public class ZookeeperTestService extends AbstractMessageTestService {

	private static Logger logger = LoggerLoader.getLogger(ZookeeperTestService.class);

	public static final String CATEGORY_PATH = "/DP/CATEGORY";
	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private ThreadLocal<CuratorClient> tlClient = new ThreadLocal<CuratorClient>() {
		protected CuratorClient initialValue() {
			try {
				return new CuratorClient(configManager.getStringValue("messageserver.address"));
			} catch (Exception e) {
				logger.error("", e);
				return null;
			}
		}
	};

	private CuratorClient getClient() {
		return tlClient.get();
	}

	private String getKeyPath(String key) {
		return CATEGORY_PATH + "/" + key;
	}

	public boolean setKeyValue(String key, String value) throws Exception {
		getClient().set(getKeyPath(key), value);
		return true;
	}

	public String getKeyValue(String key) throws Exception {
		return getClient().get(getKeyPath(key));
	}

	@Override
	public boolean removeKey(String key) throws Exception {
		getClient().delete(getKeyPath(key));
		return true;
	}

	public void randomGet() throws Exception {
		getKeyValue("k-" + (random.nextDouble() * rows));
	}

}
