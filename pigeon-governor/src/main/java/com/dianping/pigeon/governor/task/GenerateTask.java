package com.dianping.pigeon.governor.task;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.governor.task.Constants.Action;
import com.dianping.pigeon.governor.task.Constants.Environment;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;

public class GenerateTask implements Runnable {

    private static final Logger logger = Logger.getLogger(GenerateTask.class);

    private static final int INTERVAL = 30*60*1000; // 30 minutes
    private static final String ROOT = "/DP/SERVER";
    
    private HealthCheckManager manager;
    private int interval;

    public GenerateTask(HealthCheckManager manager) {
        this.manager = manager;
        interval = manager.getConfigManager().getIntValue("pigeon.healthcheck.interval", INTERVAL);
    }
    
    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try {
                logger.info("round of health check started");
                generateTasks();
                
                waitForTaskComplete();
                logger.info("round of health check finished");
                Thread.sleep(interval);
            } catch (RegistryException e) {
                logger.error("", e);
            } catch (InterruptedException e) {
                logger.warn("HealthCheckManager is interrupted", e);
            } catch(Throwable e) {
                logger.error("", e);
            }
        }
    }

    private void waitForTaskComplete() throws InterruptedException {
    	AtomicInteger n = new AtomicInteger(0);
        while(manager.getWorkerPool().getActiveCount() > 0) {
        	if(n.getAndIncrement() % 10 == 0) {
	        	String message = String.format("active threads: %d, queue size: %d, completed task: %d", 
	        			manager.getWorkerPool().getActiveCount(),
	        			manager.getWorkerPool().getQueue().size(),
	        			manager.getWorkerPool().getCompletedTaskCount());
	        	logger.info(message);
        	}
            Thread.sleep(1000);
        }
    }

    private void generateTasks() throws RegistryException {
        for(Environment env : manager.getEnvSet()) {
            generateTasks(env, manager.getAction(env));
        }
    }
    
    private void generateTasks(Environment env, Action action) throws RegistryException {
        int count = 0;

        Registry registry = manager.getRegistry(env);
        List<String> children = registry.getChildren(ROOT);
        for(String path : children) {
            if(path.startsWith("@HTTP@")) {
                count += generateTasks(env, action, path.substring(6));
            }
        }
        
        logger.info(String.format("generated %d health check tasks for env %s[%s], action is %s", count, env, env.getZkAddress(), action));
    }

    private int generateTasks(Environment env, Action action, String path) throws RegistryException {
        int count = 0;
        Registry registry = manager.getRegistry(env);
        String service = path.replace('^', '/');
        String hosts = registry.getServiceAddress(path);
        count += generateTasks(env, action, service, "", hosts);

        if(hosts != null) {
            List<String> groups = registry.getChildren(ROOT + "/" + path);
            if (groups != null && groups.size() > 0) {
                for (String group : groups) {
                    String groupPath = ROOT + "/" + path + "/" + group;
                    String groupHosts = registry.getServiceAddress(groupPath);
                    count += generateTasks(env, action, service, group, groupHosts);
                }
            }
        }
        return count;
    }

    private int generateTasks(Environment env, Action action, 
            String service, String group, String hosts) {
        int count = 0; 
        if(hosts == null)
            return count;
        
        String[] hostArray = hosts.split(",");
        for(String host : hostArray) {
            if (!StringUtils.isBlank(host)) {
                String[] ipAndPort = host.split(":");
                String ip = ipAndPort[0];
                int port = Integer.parseInt(ipAndPort[1]);
                
                CheckTask task = new CheckTask(manager, env, action, service, group, ip, port);
                manager.getWorkerPool().submit(task);
                count++;
            }
        }
        return count;
    }

}
