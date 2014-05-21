package com.dianping.pigeon.governor.task;

import java.util.ArrayList;
import java.util.List;

public class Constants { 
    
	public static final String KEY_ACTION = "pigeon.healthcheck.action";
	public static final String KEY_INTERVAL = "pigeon.healthcheck.interval";
	public static final String KEY_HOST_INTERVAL = "pigeon.healthcheck.host.interval";
	public static final String KEY_DEAD_THRESHOLD = "pigeon.healthcheck.dead.threshold";
	
	public enum Environment {
        test("127.0.0.1:2181"),
        dev("192.168.7.41:2181"),
        alpha("192.168.7.41:2182"),
        qa("192.168.213.144:2181"),
        prelease("10.2.8.143:2181"),
        product("10.1.2.32:2181,10.1.2.37:2181,10.1.2.62:2181,10.1.2.67:2181,10.1.2.58:2181"),
        performance("192.168.219.211:2181");
        
        private String zkAddress;
    
        private Environment(String zkAddress) {
            this.zkAddress = zkAddress;
        }
        
        public String getZkAddress() {
            return this.zkAddress;
        }
    }

    public enum Action {
        keep, wait, log, remove, forceRemove;
    }
    
    
    public static class Host {
    	private Service service;
    	private String ip;
        private int port;
        private volatile boolean alive;
        private volatile int deadCount;
        private volatile long lastCheckTime;
        
        public Host(Service service, String ip, int port) {
        	this.service = service;
			this.ip = ip;
			this.port = port;
	        this.alive = false;
	        this.deadCount = 0;
	        this.lastCheckTime = 0;
		}

		public Service getService() {
			return service;
		}

		public String getIp() {
			return ip;
		}

		public int getPort() {
			return port;
		}
		
		public boolean isAlive() {
			return alive;
		}

		public void setAlive(boolean alive) {
			this.alive = alive;
		}

		public int getDeadCount() {
			return deadCount;
		}

		public void setDeadCount(int deadCount) {
			this.deadCount = deadCount;
		}
		
		public void increaseDeadCount() {
			this.deadCount++;
		}

		public long getLastCheckTime() {
			return lastCheckTime;
		}

		public void updateCheckTime() {
			this.lastCheckTime = System.currentTimeMillis();
		}

		public String getAddress() {
			return ip + ":" + port;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
	    	sb.append(service).append(", address: ").append(getAddress());
	    	return sb.toString();
		}

    }
    
    public static class Service {
    	private Environment env;
        private String url;
        private String group;
        private List<Host> hostList;
        
        public Service(Environment env, String url, String group) {
        	this.env = env;
        	this.url = url;
        	this.group = group;
        	this.hostList = new ArrayList<Host>();
        }
        
        public Environment getEnv() {
			return env;
		}
        
		public String getGroup() {
			return group;
		}

		public String getUrl() {
			return url;
		}

		public void addHost(Host host) {
			hostList.add(host);
		}
		
		public List<Host> getHostList() {
			return hostList;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("env: ").append(env).append(", service: ").append(url).
			append(", group: ").append(group);
			return sb.toString();
		}

    }
    
}