package com.dianping.pigeon.governor.lion;

public enum LionKeys {

	MIN_PROVIDER_HEARTBEAT("pigeon-governor-server.min.provider.heartbeat"),

	HEARTBEAT_ENABLE("pigeon.heartbeat.enable"),

	PROVIDER_HEARTBEAT_INTERNAL("pigeon.provider.heartbeat.internal"),

	IS_ZK_DOUBLE_WRITE("pigeon-governor-server.zookeeper.isdoublewrite"),
	
	WEB_SERVERNAME("pigeon-governor-server.web.serverName"),
	
	SSO_LOUGOUT_URL("cas-server-webapp.logoutUrl");
	
	private String value;
	
	private LionKeys(String value){
		this.value = value;
	}
	
	public String value(){
		return value;
	}
}
