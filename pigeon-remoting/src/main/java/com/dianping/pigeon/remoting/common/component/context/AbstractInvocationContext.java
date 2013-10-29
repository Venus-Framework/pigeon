/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.component.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.dianping.pigeon.component.invocation.InvocationContext;
import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;

public abstract class AbstractInvocationContext implements InvocationContext {

	protected InvocationRequest request;
	protected InvocationResponse response;
	private Map<String, Serializable> contextValues;
	// 不会通过request传递到服务端，可用于filter之间传递参数
	private Map<String, Object> transientContextValues;

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
	public void putTransientContextValue(String key, Object value) {
		if (transientContextValues == null) {
			transientContextValues = new HashMap<String, Object>();
		}
		transientContextValues.put(key, value);
	}

	@Override
	public Object getTransientContextValue(String key) {
		if (transientContextValues == null) {
			return null;
		}
		return transientContextValues.get(key);
	}

	public void removeTransientContextValue(String key) {
		if (transientContextValues != null) {
			transientContextValues.remove(key);
		}
	}

}
