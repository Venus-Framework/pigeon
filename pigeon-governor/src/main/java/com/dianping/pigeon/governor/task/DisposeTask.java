package com.dianping.pigeon.governor.task;

import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.governor.util.Constants.Action;
import com.dianping.pigeon.governor.util.Constants.Host;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;

public class DisposeTask implements Runnable {

    private static final Logger logger = Logger.getLogger(DisposeTask.class);

    private HealthCheckManager manager;
    
    private HttpClient httpClient;
    
    public DisposeTask(HealthCheckManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            CheckTask result;
            try {
                result = manager.getResultQueue().take();
                processResult(result);
            } catch (RegistryException e) {
                logger.error("", e);
            } catch (InterruptedException e) {
                logger.warn("DisposeTask is interrupted", e);
            } catch(Throwable e) {
                logger.error("", e);
            }
        }
    }

    private void processResult(CheckTask task) throws RegistryException, InterruptedException {
        if(task == null)
            return;
        if(task.getHost().isAlive())
            return;
        
        if(task.getHost().getDeadCount() >= manager.getDeadThreshold()) {
            disposeAddress(task);
        } else {
            checkAgain(task);
        }
    }

    private void disposeAddress(CheckTask result) throws RegistryException, InterruptedException {
        switch(determineDisposeAction(result.getHost())) {
        case log:
            logAddress(result);
            return;
        case remove:
        	removeAddress(result);
            return;
        case wait:
        	Thread.sleep(20);
        	manager.getResultQueue().add(result);
        	return;
        case keep:
        	logger.info("keep dead server " + result.getHost());
            return;
        default:
            return;
        }
    }
    
    private Action determineDisposeAction(Host host) {
		int n = canRemoveHost(host.getService().getHostList(), host);
		if(n == 0)
			return Action.wait;
		if(n > 0)
			return manager.getAction(host.getService().getEnv());
		return Action.keep;
	}

    /*
     * 1. will not remove if only 1 host exists
     * 2. will not remove if all hosts are dead
     * 3. will remove if at least one host is alive
     */
	private int canRemoveHost(List<Host> hostList, Host host) {
		if(hostList.size() <= 1)
			return -1;
		boolean hasLiveHost = false;
		boolean isChecking = false;
		for(Host hh : hostList) {
			if(hh.isAlive()) {
				hasLiveHost = true;
			} else {
				if(hh.getDeadCount() < manager.getDeadThreshold())
					isChecking = true;
			}
		}
		return hasLiveHost ? 1 : (isChecking ? 0 : -1);
	}

	private void logAddress(CheckTask task) {
    	logger.info("log dead server " + task.getHost());
    }

    private void removeAddress(CheckTask task) throws RegistryException {
    	Host host = task.getHost();
        Registry registry = manager.getRegistry(host.getService().getEnv());
        registry.unregisterService("@HTTP@" + host.getService().getUrl(), host.getService().getGroup(), host.toString());
        registry.unregisterService(host.getService().getUrl(), host.getService().getGroup(), host.toString());
        notifyLionApi(task);
    }
    
    private void notifyLionApi(CheckTask task) {
        String url = generateUrl(task);
        try {
            String message = doHttpGet(url);
            if(message.startsWith("0|")) {
                logger.info("removed address " + task.getHost());
            } else {
                logger.error("failed to remove address " + task.getHost() + 
                        ", message: "+ message);
            }
        } catch (IOException e) {
            logger.error("failed to remove address " + task.getHost(), e);
        }
    }
    
    private void checkAgain(CheckTask task) {
        manager.getWorkerPool().submit(task);
    }

    private String doHttpGet(String url) throws IOException {
        GetMethod get = new GetMethod(url);
        HttpClient httpClient = getHttpClient();
        try {
            httpClient.executeMethod(get);
            return get.getResponseBodyAsString();
        } finally {
            get.releaseConnection();
        }
    }

    private String generateUrl(CheckTask task) {
    	Host host = task.getHost();
        StringBuilder sb = new StringBuilder("http://lionapi.dp:8080/service/unpublish?id=3");
        sb.append('&').append("env=").append(host.getService().getEnv().name());
        sb.append('&').append("service=").append(host.getService().getUrl());
        if(StringUtils.isNotBlank(host.getService().getGroup()))
            sb.append('&').append("group=").append(host.getService().getGroup());
        sb.append('&').append("ip=").append(host.getIp());
        sb.append('&').append("port=").append(host.getPort());
        sb.append('&').append("updatezk=false");
        return sb.toString();
    }
    
    private HttpClient getHttpClient() {
        if(httpClient == null) {
            HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
            HttpConnectionManagerParams params = new HttpConnectionManagerParams();
            params.setMaxTotalConnections(500);
            params.setDefaultMaxConnectionsPerHost(10);
            params.setConnectionTimeout(3000);
            params.setTcpNoDelay(true);
            params.setSoTimeout(3000);
            params.setStaleCheckingEnabled(true);
            connectionManager.setParams(params);
            
            httpClient = new HttpClient(connectionManager);
        }
        return httpClient;
    }

}
