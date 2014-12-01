package com.dianping.pigeon.console.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dianping.pigeon.console.domain.Service;

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

	private Map<String, String> heartbeats;

	private Map<String, String> reconnects;

	private String group;

	private String direct = "false";

	private Map<String, Integer> serviceWeights;

	private String error = "";

	private String registry = "";

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

	public Map<String, String> getHeartbeats() {
		return heartbeats;
	}

	public void setHeartbeats(Map<String, String> heartbeats) {
		this.heartbeats = heartbeats;
	}

	public Map<String, String> getReconnects() {
		return reconnects;
	}

	public void setReconnects(Map<String, String> reconnects) {
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
