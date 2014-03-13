package com.dianping.pigeon.console.servlet;

import java.util.ArrayList;
import java.util.List;

import com.dianping.pigeon.console.domain.Service;

public class ServicePage {
	private int port;

	private List<Service> services;

	private String status = "";

	private String env = "";

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
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
		if (services == null) {
			services = new ArrayList<Service>();
		}
		services.add(s);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
