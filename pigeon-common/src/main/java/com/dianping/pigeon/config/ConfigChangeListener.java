package com.dianping.pigeon.config;


public interface ConfigChangeListener {

	void onKeyUpdated(String key, String value);

	void onKeyAdded(String key, String value);

	void onKeyRemoved(String key);

}
