package com.dianping.pigeon.governor.task;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.governor.task.Constants.Action;
import com.dianping.pigeon.governor.task.Constants.Environment;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;

public class HealthCheckManager extends Thread {

    private static final Logger logger = Logger.getLogger(HealthCheckManager.class);
    
    private static final String DEFAULT_ACTION = "alpha:remove,qa:log";
    
    private Registry registryClass = ExtensionLoader.getExtension(Registry.class);
    
    private BlockingQueue<CheckTask> resultQueue = new LinkedBlockingQueue<CheckTask>();
    
    private ThreadPoolExecutor workerPool;
    private ExecutorService bossPool;
    
    private String action;
    private Map<Environment, Action> actionMap;
    private Map<Environment, Registry> registryMap;
    
    private ConfigManager configManager;
    
    public HealthCheckManager() {
        configManager = ExtensionLoader.getExtension(ConfigManager.class);
        action = configManager.getStringValue("pigeon.healthcheck.action", DEFAULT_ACTION);
        actionMap = new LinkedHashMap<Environment, Action>();
        registryMap = new LinkedHashMap<Environment, Registry>();
        parseAction();
    }
    
    public void run() {
        workerPool = new ThreadPoolExecutor(4, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        
        bossPool = Executors.newFixedThreadPool(2);
        bossPool.submit(new GenerateTask(this));
        bossPool.submit(new DisposeTask(this));
    }

    private void parseAction() {
        if(StringUtils.isBlank(action)) {
            logger.error("action is null");
            return;
        }
        
        String[] envActionList = action.split(",");
        for(String envAction : envActionList) {
            String[] envActionPair = envAction.split(":");
            Environment env = Environment.valueOf(envActionPair[0].trim());
            Action act = Action.valueOf(envActionPair[1].trim());
            actionMap.put(env, act);
        }
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
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
    
    public synchronized Registry getRegistry(Environment env) throws RegistryException {
        Registry registry = registryMap.get(env);
        if(registry == null) {
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
        props.put(Constants.KEY_REGISTRY_ADDRESS, env.getZkAddress());
        registry.init(props);
        return registry;
    }
    
    public static void main(String[] args) {
        DOMConfigurator.configureAndWatch(HealthCheckManager.class.getClassLoader().getResource("log4j.xml").getFile());
        
        new HealthCheckManager().start();
        logger.info("HealthCheckManager started");
    }

}
