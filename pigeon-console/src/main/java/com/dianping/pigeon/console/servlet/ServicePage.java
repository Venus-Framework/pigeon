package com.dianping.pigeon.console.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.pigeon.console.domain.Service;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.remoting.invoker.Client;

public class ServicePage {
	private String startTime = "";

	private String appName = "";

	private String port = "";

	private int httpPort;

	private List<Service> services = new ArrayList<Service>();

	private String status = "";

	private String environment = "";

	private String published = "";

	private String online = "";

	private String phase = "";

	private List<String> invokers;

	private Map<String, List<ClientInfo>> heartbeats;

	private Map<String, ClientInfo> reconnects;

	private String group;

	private String direct = "false";

	private Map<String, Integer> serviceWeights;

	private String error = "";

	private String registry = "";

	private String validate = "false";

	Map<String, Set<HostInfo>> serviceAddresses;

	public Map<String, Set<HostInfo>> getServiceAddresses() {
		return serviceAddresses;
	}

	public void setServiceAddresses(Map<String, Set<HostInfo>> serviceAddresses) {
		this.serviceAddresses = serviceAddresses;
	}

	public String getValidate() {
		return validate;
	}

	public void setValidate(String validate) {
		this.validate = validate;
	}

	public static class ClientInfo {
		private Client client;
		private int weight;

		public ClientInfo(Client client, int weight) {
			this.client = client;
			this.weight = weight;
		}

		public Client getClient() {
			return client;
		}

		public void setClient(Client client) {
			this.client = client;
		}

		public int getWeight() {
			return weight;
		}

		public void setWeight(int weight) {
			this.weight = weight;
		}

		public String toString() {
			return client + ", weight:" + weight;
		}
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getRegistry() {
		return registry;
	}

	public void setRegistry(String registry) {
		this.registry = registry;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String getOnline() {
		return online;
	}

	public void setOnline(String online) {
		this.online = online;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Map<String, Integer> getServiceWeights() {
		return serviceWeights;
	}

	public void setServiceWeights(Map<String, Integer> serviceWeights) {
		this.serviceWeights = serviceWeights;
	}

	public String getDirect() {
		return direct;
	}

	public void setDirect(String direct) {
		this.direct = direct;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Map<String, List<ClientInfo>> getHeartbeats() {
		return heartbeats;
	}

	public void setHeartbeats(Map<String, List<ClientInfo>> heartbeats) {
		this.heartbeats = heartbeats;
	}

	public Map<String, ClientInfo> getReconnects() {
		return reconnects;
	}

	public void setReconnects(Map<String, ClientInfo> reconnects) {
		this.reconnects = reconnects;
	}

	public List<String> getInvokers() {
		return invokers;
	}

	public void setInvokers(List<String> invokers) {
		this.invokers = invokers;
	}

	public String getPublished() {
		return published;
	}

	public void setPublished(String published) {
		this.published = published;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}

	public void addService(Service s) {
		services.add(s);
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

}
