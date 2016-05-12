package com.dianping.pigeon.console.domain;

import java.util.ArrayList;
import java.util.List;

public class Service {
	private String name;

	private Class<?> type;

	private String published;

	private String token = "";

	private List<ServiceMethod> methods = new ArrayList<ServiceMethod>();

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPublished() {
		return published;
	}

	public void setPublished(String published) {
		this.published = published;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ServiceMethod> getMethods() {
		return this.methods;
	}

	public void setMethods(List<ServiceMethod> methods) {
		this.methods = methods;
	}

	public void addMethod(ServiceMethod serviceMethod) {
		if (this.methods == null) {
			this.methods = new ArrayList<ServiceMethod>();
		}
		this.methods.add(serviceMethod);
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

}
