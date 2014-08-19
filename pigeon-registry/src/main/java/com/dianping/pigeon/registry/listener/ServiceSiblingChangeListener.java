package com.dianping.pigeon.registry.listener;

public interface ServiceSiblingChangeListener {

	void siblingAdded(String serviceName, String host);

	void siblingRemoved(String serviceName, String host);

	void siblingWeightChanged(String serviceName, String host);
}
