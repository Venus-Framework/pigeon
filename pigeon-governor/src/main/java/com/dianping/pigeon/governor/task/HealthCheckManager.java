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
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.Constants.Action;
import com.dianping.pigeon.governor.util.Constants.Environment;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;

public class HealthCheckManager extends Thread {

	private static final Logger logger = Logger.getLogger(HealthCheckManager.class);

	private Registry registryClass = ExtensionLoader.getExtension(Registry.class);

	private int corePoolSize = Runtime.getRuntime().availableProcessors();
	private int maxPoolSize = corePoolSize * 10;
	private int queueSize = 500;

	private BlockingQueue<CheckTask> resultQueue = new LinkedBlockingQueue<CheckTask>();

	private ThreadPoolExecutor workerPool;
	private ExecutorService bossPool;

	private String action;
	private volatile long interval = 10 * 1000;
	private volatile long hostInterval = 5 * 1000;
	private volatile int deadThreshold = 10;

	private Map<Environment, Action> actionMap;
	private Map<Environment, Registry> registryMap;

	private ConfigCache configManager;

	public HealthCheckManager() {
		try {
			configManager = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress());
			configManager.addChange(new ConfigChangeHandler());
			action = configManager.getProperty(Constants.KEY_ACTION);
			String tmp = configManager.getProperty(Constants.KEY_INTERVAL);
			interval = Long.parseLong(tmp);
			tmp = configManager.getProperty(Constants.KEY_HOST_INTERVAL);
			hostInterval = Long.parseLong(tmp);
			tmp = configManager.getProperty(Constants.KEY_DEAD_THRESHOLD);
			deadThreshold = Integer.parseInt(tmp);
		} catch (Exception e) {
			logger.error("", e);
			action = "dev:remove";
			interval = 10 * 1000;
			hostInterval = 5 * 1000;
			deadThreshold = 10;
		}
		actionMap = new LinkedHashMap<Environment, Action>();
		registryMap = new LinkedHashMap<Environment, Registry>();
		parseAction();
	}

	public void run() {
		workerPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(queueSize), new NamingThreadFactory("pigeon-healthcheck"),
				new BlockProviderPolicy());

		bossPool = Executors.newFixedThreadPool(2);
		bossPool.submit(new GenerateTask(this));
		bossPool.submit(new DisposeTask(this));
	}

	private void parseAction() {
		if (StringUtils.isBlank(action)) {
			logger.error("action is null");
			return;
		}

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

	public long getHostInterval() {
		return hostInterval;
	}

	public int getDeadThreshold() {
		return deadThreshold;
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

	class ConfigChangeHandler implements ConfigChange {

		@Override
		public void onChange(String key, String value) {
			if (Constants.KEY_ACTION.equals(key)) {
				action = value;
				parseAction();
			} else if (Constants.KEY_INTERVAL.equals(key)) {
				interval = Long.parseLong(value);
			} else if (Constants.KEY_HOST_INTERVAL.equals(key)) {
				hostInterval = Long.parseLong(value);
			} else if (Constants.KEY_DEAD_THRESHOLD.equals(key)) {
				deadThreshold = Integer.parseInt(value);
			}
		}

	}
}
