/**
 * 
 */
package com.dianping.pigeon.console.servlet.json;

import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

/**
 * @author sean.wang
 * @since Jul 17, 2012
 */
public class ServiceJsonServlet extends ServiceServlet {

	private static final long serialVersionUID = -3000545547453006628L;

	public ServiceJsonServlet(ServerConfig serverConfig, int port) {
		super(serverConfig, port);
	}

	@Override
	public String getView() {
		return "ServiceJson.ftl";
	}

	public String getContentType() {
		return "application/json; charset=UTF-8";
	}
}
