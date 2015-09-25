package com.dianping.pigeon.governor.lion;

public enum LionKeys {

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
