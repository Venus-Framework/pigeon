package com.dianping.pigeon.governor.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.dianping.pigeon.config.ConfigManagerLoader;

public class Constants {

	public static final String KEY_ACTION = "pigeon.governor.healthcheck.action";
	public static final String KEY_INTERVAL = "pigeon.governor.healthcheck.interval";
	public static final String KEY_HOST_INTERVAL = "pigeon.governor.healthcheck.host.interval";
	public static final String KEY_DEAD_THRESHOLD = "pigeon.governor.healthcheck.dead.threshold";
	public static final String KEY_MINHOSTS = "pigeon.governor.healthcheck.minhosts";
	public static final String KEY_DEADTHRESHOLDS = "pigeon.governor.healthcheck.deadthreshold";
	public static final String KEY_INVALIDADDRESS = "pigeon.governor.healthcheck.invalidaddress";

	private static final String testAddress = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.governor.address.test", "127.0.0.1:2181");
	private static final String devAddress = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.governor.address.dev", "192.168.7.41:2181");
	private static final String alphaAddress = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.governor.address.alpha", "192.168.7.41:2182");
	private static final String qaAddress = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.governor.address.qa", "192.168.213.144:2181");
	private static final String preleaseAddress = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.governor.address.prelease", "10.2.8.143:2181");
	private static final String productAddress = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.governor.address.productnh",
			"10.1.2.32:2181,10.1.2.37:2181,10.1.2.62:2181,10.1.2.67:2181,10.1.2.58:2181");
	private static final String performanceAddress = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.governor.address.performance", "192.168.219.211:2181");
	private static final String producthmAddress = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.governor.address.producthm", "10.2.21.141:2181,10.2.26.107:2181,10.2.26.146:2181");

	public enum Environment {
		test(testAddress), dev(devAddress), alpha(alphaAddress), qa(qaAddress), prelease(preleaseAddress), product(
				productAddress), performance(performanceAddress), producthm(producthmAddress);

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
		private volatile boolean checkResponse = true;

		public Host(Service service, String ip, int port) {
			this.service = service;
			this.ip = ip;
			this.port = port;
			this.alive = false;
			this.deadCount = 0;
			this.lastCheckTime = 0;
		}

		public boolean isCheckResponse() {
			return checkResponse;
		}

		public void setCheckResponse(boolean checkResponse) {
			this.checkResponse = checkResponse;
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
			sb.append(getAddress()).append(",").append(service).append(",dead:").append(deadCount);
			return sb.toString();
		}

		public boolean equals(Object an) {
			return new EqualsBuilder().append(this.getAddress(), ((Host) an).getAddress()).isEquals();
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
			if (!hostList.contains(host)) {
				hostList.add(host);
			}
		}

		public List<Host> getHostList() {
			return hostList;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("env: ").append(env).append(", service: ").append(url).append(", group: ").append(group);
			return sb.toString();
		}

	}

}