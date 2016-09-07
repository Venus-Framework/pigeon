package com.dianping.pigeon.registry.listener;

import java.util.Map;

public interface ServerInfoListener {

	void onServerAppChange(String serverAddress, String app);

	void onServerVersionChange(String serverAddress, String version);

	void onServerProtocolChange(String serverAddress, Map<String, Boolean> protocolInfoMap);

	void onServerHeartBeatSupportChange(String serverAddress, byte heartBeatSupport);
	
}
