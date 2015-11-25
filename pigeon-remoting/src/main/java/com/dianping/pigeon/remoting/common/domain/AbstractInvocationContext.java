/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractInvocationContext implements InvocationContext {

	protected InvocationRequest request;
	protected InvocationResponse response;
	private Map<String, Serializable> contextValues;
	private List<Long> timeline = new ArrayList<Long>();

	public AbstractInvocationContext(InvocationRequest request) {
		this.request = request;
	}

	@Override
	public InvocationRequest getRequest() {
		return request;
	}

	public void setRequest(InvocationRequest request) {
		this.request = request;
	}

	@Override
	public InvocationResponse getResponse() {
		return response;
	}

	public void setResponse(InvocationResponse response) {
		this.response = response;
	}

	@Override
	public void putContextValue(String key, Serializable value) {
		if (contextValues == null) {
			contextValues = new HashMap<String, Serializable>();
		}
		contextValues.put(key, value);
	}

	@Override
	public Serializable getContextValue(String key) {
		if (contextValues == null) {
			return null;
		}
		return contextValues.get(key);
	}

	@Override
	public Map<String, Serializable> getContextValues() {
		return contextValues;
	}

	@Override
	public List<Long> getTimeline() {
		return timeline;
	}
}
