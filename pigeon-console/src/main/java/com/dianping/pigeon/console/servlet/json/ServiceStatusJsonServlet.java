/**
 * 
 */
package com.dianping.pigeon.console.servlet.json;

import com.dianping.pigeon.console.servlet.ServiceServlet;

/**
 */
public class ServiceStatusJsonServlet extends ServiceServlet {

	private static final long serialVersionUID = -3000545547453006628L;

	public ServiceStatusJsonServlet(int port) {
		super(port);
	}

	@Override
	public String getView() {
		return "ServiceStatusJson.ftl";
	}

	public String getContentType() {
		return "application/json; charset=UTF-8";
	}
}
