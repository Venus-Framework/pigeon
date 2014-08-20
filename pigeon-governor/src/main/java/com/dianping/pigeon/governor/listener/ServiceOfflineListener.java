package com.dianping.pigeon.governor.listener;

public interface ServiceOfflineListener {

	void offline(String serviceName, String host, String group);

}
