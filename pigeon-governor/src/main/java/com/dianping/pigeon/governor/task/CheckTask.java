package com.dianping.pigeon.governor.task;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.pigeon.governor.task.Constants.Action;
import com.dianping.pigeon.governor.task.Constants.Environment;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.domain.CallbackFuture;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;
import com.dianping.pigeon.remoting.netty.invoker.NettyClient;

public class CheckTask implements Runnable {

    private static final Logger logger = Logger.getLogger(CheckTask.class);
    
    private static final long HOST_INTERVAL = 60*1000; 

    private HealthCheckManager manager;
    private Environment env;
    private Action action;
    private String service;
    private String group;
    private String ip;
    private int port;
    private volatile boolean alive;
    private volatile int deadCount;
    private volatile long lastCheckTime;
    private long hostInterval;

    public CheckTask(HealthCheckManager manager, Environment env, Action action, 
            String service, String group, String ip, int port) {
        this.manager = manager;
        this.env = env;
        this.action = action;
        this.service = service;
        this.group = group;
        this.ip = ip;
        this.port = port;
        this.alive = false;
        this.deadCount = 0;
        this.lastCheckTime = 0;
        hostInterval = manager.getConfigManager().getLongValue("pigeon.healthcheck.host.interval", HOST_INTERVAL);
    }

    @Override
    public void run() {
        try {
            checkServer();
        } catch (InterruptedException e) {
            logger.warn("CheckTask is interrupted");
        } catch(Throwable e) {
            logger.error("", e);
        }
    }

    private void checkServer() throws InterruptedException {
        if(System.currentTimeMillis() - lastCheckTime > hostInterval) {
            if(isServerAlive()) {
                alive = true;
                deadCount = 0;
            } else {
                alive = false;
                if(deadCount < Integer.MAX_VALUE)
                    deadCount++;
            }
            lastCheckTime = System.currentTimeMillis();
            manager.getResultQueue().add(this);
        } else {
            manager.getWorkerPool().submit(this);
            Thread.sleep(100);
        }
    }
    
    private boolean isServerAlive() {
        boolean alive = false;
        try {
            InvocationRequest request = createHealthCheckRequest();
            InvocationResponse response = getHealthCheckResponse(request);
            if (response != null) {
                alive = true;
                // if response.getReturn() is an exception, consider server alive
                if(response.getReturn() instanceof Map) {
                    Map result = (Map) response.getReturn();
                    if (result != null) {
                        logger.info("server " + getAddress() + " response: " + result);
                        
                        // If group does not match, remove immediately
                        if(result.containsKey("group") && !isSameGroup(group, (String) result.get("group"))) {
                            alive = false;
                            deadCount = Integer.MAX_VALUE;
                        }
                    }
                }
            }
        } catch(NetTimeoutException e) {
            logger.error("server " + getAddress() + " timeout, dead count " + deadCount + ": " + e.getMessage());
            alive = false;
        } catch (Throwable t) {
            logger.error("error contacting server " + getAddress() + ", dead count " + deadCount, t);
            alive = false;
        }
        return alive;
    }

    private boolean isSameGroup(String group1, String group2) {
        if(StringUtils.isEmpty(group1))
            return StringUtils.isEmpty(group2);
        else
            return group1.equals(group2);
    }
    
    private InvocationRequest createHealthCheckRequest() {
        InvocationRequest request = new DefaultRequest("", "", null, SerializerFactory.SERIALIZE_HESSIAN,
                Constants.MESSAGE_TYPE_HEALTHCHECK, 3000, null);
        request.setSequence(1);
        request.setCreateMillisTime(System.currentTimeMillis());
        request.setCallType(Constants.CALLTYPE_REPLY);
        return request;
    }
    
    private InvocationResponse getHealthCheckResponse(InvocationRequest request) throws InterruptedException {
        NettyClient client = null;
        try {
            ConnectInfo connectInfo = new ConnectInfo(service, ip, port, 1);
            client = new NettyClient(connectInfo);
            client.connect();
            CallbackFuture future = new CallbackFuture();
            future.setRequest(request);
            future.setClient(client);
            InvokerUtils.sendRequest(client, request, future);
            InvocationResponse response = future.get(request.getTimeout());
            return response;
        } finally {
            try {
                client.close();
            } catch (Throwable t) {
            }
        }
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public String getAddress() {
        return ip + ":" + port;
    }

    public int getDeadCount() {
        return deadCount;
    }

}
