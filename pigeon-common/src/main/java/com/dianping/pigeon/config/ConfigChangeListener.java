package com.dianping.pigeon.config;

import java.util.Map;

public interface ConfigChangeListener {

	void onKeyUpdated(String key, String value);

	void onKeyAdded(String key, String value);

	void onKeyRemoved(String key);

	void onConfigChange(Map<String, Object> properties);
}
