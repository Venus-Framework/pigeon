/**
 * 
 */
package com.dianping.pigeon.console.servlet.json;

import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

/**
 */
public class ServiceStatusJsonServlet extends ServiceServlet {

	private static final long serialVersionUID = -3000545547453006628L;

	public ServiceStatusJsonServlet(ServerConfig serverConfig, int port) {
		super(serverConfig, port);
	}

	@Override
	public String getView() {
		return "ServiceStatusJson.ftl";
	}

	public String getContentType() {
		return "application/json; charset=UTF-8";
	}
}
