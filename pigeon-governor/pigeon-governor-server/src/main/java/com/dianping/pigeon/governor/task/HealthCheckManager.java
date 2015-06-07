package com.dianping.pigeon.governor.task;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.Constants.Action;
import com.dianping.pigeon.governor.util.Constants.Environment;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.util.NetUtils;

public class HealthCheckManager extends Thread {

	private static final Logger logger = LoggerLoader.getLogger(HealthCheckManager.class);

	private Registry registryClass = ExtensionLoader.getExtension(Registry.class);

	private int corePoolSize = Runtime.getRuntime().availableProcessors();
	private int maxPoolSize = corePoolSize * 10;
	private int queueSize = 500;

	private BlockingQueue<CheckTask> resultQueue = new LinkedBlockingQueue<CheckTask>();

	private ThreadPoolExecutor workerPool;
	private ExecutorService bossPool;

	private String action;
	private String minhosts;
	private String deadThresholds;
	private String invalidAddress;
	private volatile long interval = 10 * 1000;
	private volatile long hostInterval = 5 * 1000;
	private volatile int deadThreshold = 50;

	private Map<Environment, Action> actionMap;
	private Map<Environment, Integer> minhostsMap;
	private Map<Environment, Integer> deadThresholdsMap;
	private Map<Environment, Registry> registryMap;
	private Map<Environment, String> invalidAddressMap;

	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	public HealthCheckManager() {
		configManager.registerConfigChangeListener(new ConfigChangeHandler());
		action = configManager.getStringValue(Constants.KEY_ACTION, "dev:remove");
		interval = configManager.getLongValue(Constants.KEY_INTERVAL, 10 * 1000);
		hostInterval = configManager.getLongValue(Constants.KEY_HOST_INTERVAL, 5 * 1000);
		deadThreshold = configManager.getIntValue(Constants.KEY_DEAD_THRESHOLD, 50);
		minhosts = configManager.getStringValue(Constants.KEY_MINHOSTS, "qa:1,prelease:1,product:2,producthm:1");
		deadThresholds = configManager.getStringValue(Constants.KEY_DEADTHRESHOLDS,
				"dev:10,alpha:10,qa:20,prelease:20,product:50,producthm:50");
		invalidAddress = configManager.getStringValue(Constants.KEY_INVALIDADDRESS, "product:192.168|10.128");
		actionMap = new LinkedHashMap<Environment, Action>();
		registryMap = new LinkedHashMap<Environment, Registry>();
		minhostsMap = new LinkedHashMap<Environment, Integer>();
		deadThresholdsMap = new LinkedHashMap<Environment, Integer>();
		invalidAddressMap = new LinkedHashMap<Environment, String>();
		parseAction();
		parseMinhosts();
		parseDeadThresholds();
		parseInvalidAddress();
	}

	public void run() {
		boolean enable = false;
		if ("product".equalsIgnoreCase(configManager.getEnv())) {
			String ip = configManager.getStringValue("pigeon-governor-server.enable.ip", "");
			if (!NetUtils.getFirstLocalIp().equals(ip)) {
				enable = false;
			}
		}
		if (enable) {
			workerPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS,
					new ArrayBlockingQueue<Runnable>(queueSize), new NamingThreadFactory("pigeon-healthcheck"),
					new BlockProviderPolicy());

			bossPool = Executors.newFixedThreadPool(2);
			bossPool.submit(new GenerateTask(this));
			bossPool.submit(new DisposeTask(this));
		}
	}

	private void parseAction() {
		if (StringUtils.isBlank(action)) {
			logger.error("action is null");
			return;
		}

		actionMap.clear();
		String[] envActionList = action.split(",");
		for (String envAction : envActionList) {
			String[] envActionPair = envAction.split(":");
			String strEnv = envActionPair[0].trim();
			if (strEnv.indexOf("-") != -1) {
				strEnv = strEnv.replace("-", "");
			}
			Environment env = Environment.valueOf(strEnv);
			Action act = Action.valueOf(envActionPair[1].trim());
			actionMap.put(env, act);
		}
	}

	private void parseMinhosts() {
		if (StringUtils.isBlank(minhosts)) {
			logger.error("minhosts is null");
			return;
		}

		String[] envActionList = minhosts.split(",");
		for (String envAction : envActionList) {
			String[] envActionPair = envAction.split(":");
			String strEnv = envActionPair[0].trim();
			if (strEnv.indexOf("-") != -1) {
				strEnv = strEnv.replace("-", "");
			}
			Environment env = Environment.valueOf(strEnv);
			int min = Integer.valueOf(envActionPair[1].trim());
			minhostsMap.put(env, min);
		}
	}

	private void parseInvalidAddress() {
		if (StringUtils.isBlank(invalidAddress)) {
			logger.error("invalidAddress is null");
			return;
		}

		String[] invalidAddressList = invalidAddress.split(",");
		for (String envInvalidAddr : invalidAddressList) {
			if (envInvalidAddr.length() > 0) {
				String[] envInvalidAddrPair = envInvalidAddr.split(":");
				String strEnv = envInvalidAddrPair[0].trim();
				Environment env = Environment.valueOf(strEnv);
				String addr = envInvalidAddrPair[1].trim();
				invalidAddressMap.put(env, addr);
			}
		}
	}

	private void parseDeadThresholds() {
		if (StringUtils.isBlank(deadThresholds)) {
			logger.error("deadThresholds is null");
			return;
		}

		String[] envActionList = deadThresholds.split(",");
		for (String envAction : envActionList) {
			String[] envActionPair = envAction.split(":");
			String strEnv = envActionPair[0].trim();
			if (strEnv.indexOf("-") != -1) {
				strEnv = strEnv.replace("-", "");
			}
			Environment env = Environment.valueOf(strEnv);
			int threshold = Integer.valueOf(envActionPair[1].trim());
			deadThresholdsMap.put(env, threshold);
		}
	}

	public ThreadPoolExecutor getWorkerPool() {
		return workerPool;
	}

	public BlockingQueue<CheckTask> getResultQueue() {
		return resultQueue;
	}

	public Set<Environment> getEnvSet() {
		return actionMap.keySet();
	}

	public Action getAction(Environment env) {
		return actionMap.get(env);
	}

	public int getMinhosts(Environment env) {
		if (minhostsMap.containsKey(env)) {
			return minhostsMap.get(env);
		} else {
			return 0;
		}
	}

	public int getDeadThreshold(Environment env) {
		if (deadThresholdsMap.containsKey(env)) {
			return deadThresholdsMap.get(env);
		} else {
			return deadThreshold;
		}
	}

	public String getInvalidAddress(Environment env) {
		if (invalidAddressMap.containsKey(env)) {
			return invalidAddressMap.get(env);
		} else {
			return null;
		}
	}

	public long getHostInterval() {
		return hostInterval;
	}

	public long getInterval() {
		return interval;
	}

	public synchronized Registry getRegistry(Environment env) throws RegistryException {
		Registry registry = registryMap.get(env);
		if (registry == null) {
			try {
				registry = initRegistry(env);
				registryMap.put(env, registry);
			} catch (Exception e) {
				String message = String.format("failed to init registry for %s[%s]", env.name(), env.getZkAddress());
				logger.error(message, e);
				throw new RegistryException(message, e);
			}
		}
		return registry;
	}

	private Registry initRegistry(Environment env) throws InstantiationException, IllegalAccessException {
		Registry registry = registryClass.getClass().newInstance();
		Properties props = new Properties();
		props.put(com.dianping.pigeon.registry.util.Constants.KEY_REGISTRY_ADDRESS, env.getZkAddress());
		registry.init(props);
		return registry;
	}

	public static void main(String[] args) {
		DOMConfigurator.configureAndWatch(HealthCheckManager.class.getClassLoader().getResource("log4j.xml").getFile());

		new HealthCheckManager().start();
		logger.info("HealthCheckManager started");
	}

	class BlockProviderPolicy implements RejectedExecutionHandler {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			if (!executor.isShutdown()) {
				try {
					executor.getQueue().put(r);
				} catch (InterruptedException e) {
					// should not be interrupted
				}
			}
		}

	}

	class NamingThreadFactory implements ThreadFactory {

		private String prefix;
		private AtomicInteger n;

		public NamingThreadFactory(String prefix) {
			if (prefix == null) {
				throw new NullPointerException("Thread name prefix is null");
			}
			this.prefix = prefix;
			this.n = new AtomicInteger(0);
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, prefix + "-" + n.incrementAndGet());
			t.setDaemon(true);
			return t;
		}

	}

	class ConfigChangeHandler implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (Constants.KEY_ACTION.equals(key)) {
				action = value;
				parseAction();
			} else if (Constants.KEY_INTERVAL.equals(key)) {
				interval = Long.parseLong(value);
			} else if (Constants.KEY_HOST_INTERVAL.equals(key)) {
				hostInterval = Long.parseLong(value);
			} else if (Constants.KEY_DEAD_THRESHOLD.equals(key)) {
				deadThreshold = Integer.parseInt(value);
			} else if (Constants.KEY_MINHOSTS.equals(key)) {
				minhosts = value;
				parseMinhosts();
			} else if (Constants.KEY_DEADTHRESHOLDS.equals(key)) {
				deadThresholds = value;
				parseDeadThresholds();
			} else if (Constants.KEY_INVALIDADDRESS.equals(key)) {
				invalidAddress = value;
				parseInvalidAddress();
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {
		}

		@Override
		public void onKeyRemoved(String key) {
		}

	}
}
