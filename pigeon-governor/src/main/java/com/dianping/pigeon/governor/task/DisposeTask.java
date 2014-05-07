package com.dianping.pigeon.governor.task;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;

public class DisposeTask implements Runnable {

    private static final Logger logger = Logger.getLogger(DisposeTask.class);

    private static final int THRESHOLD = 5;
    
    private HealthCheckManager manager;
    
    private HttpClient httpClient;
    private int threshold;
    
    public DisposeTask(HealthCheckManager manager) {
        this.manager = manager;
        threshold = manager.getConfigManager().getIntValue("pigeon.healthcheck.dead.threshold", THRESHOLD);
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

    private void processResult(CheckTask result) throws RegistryException {
        if(result == null)
            return;
        if(result.isAlive())
            return;
        
        if(result.getDeadCount() >= threshold) {
            disposeAddress(result);
        } else {
            checkAgain(result);
        }
    }

    private void disposeAddress(CheckTask result) throws RegistryException {
        switch(result.getAction()) {
        case log:
            logAddress(result);
            return;
        case remove:
            removeAddress(result);
            return;
        case none:
            return;
        default:
            return;
        }
    }
    
    private void logAddress(CheckTask result) {
        logger.info("find dead server " + result.getAddress());
    }
    
    private void removeAddress(CheckTask result) throws RegistryException {
        Registry registry = manager.getRegistry(result.getEnv());
        registry.unregisterService("@HTTP@"+result.getService(), result.getGroup(), result.getAddress());
        registry.unregisterService(result.getService(), result.getGroup(), result.getAddress());
        notifyLionApi(result);
    }
    
    private void notifyLionApi(CheckTask result) {
        String url = generateUrl(result);
        try {
            String message = doHttpGet(url);
            if(message.startsWith("0|")) {
                logger.info("removed address " + result.getAddress());
            } else {
                logger.error("failed to remove address " + result.getAddress() + 
                        ", message: "+ message);
            }
        } catch (IOException e) {
            logger.error("failed to remove address " + result.getAddress(), e);
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
        StringBuilder sb = new StringBuilder("http://lionapi.dp:8080/service/unpublish?id=3");
        sb.append('&').append("env=").append(task.getEnv().name());
        sb.append('&').append("service=").append(task.getService());
        if(StringUtils.isNotBlank(task.getGroup()))
            sb.append('&').append("group=").append(task.getGroup());
        sb.append('&').append("ip=").append(task.getIp());
        sb.append('&').append("port=").append(task.getPort());
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
