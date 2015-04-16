package com.dianping.pigeon.registry.listener;

public interface ServerInfoListener {

	void onServerAppChange(String serverAddress, String app);

	void onServerVersionChange(String serverAddress, String version);
}
