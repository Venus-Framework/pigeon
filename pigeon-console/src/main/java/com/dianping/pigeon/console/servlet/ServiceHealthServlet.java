/**
 * 
 */
package com.dianping.pigeon.console.servlet;



public class ServiceHealthServlet extends ServiceServlet {

	private static final long serialVersionUID = -3000545547453006628L;

	public ServiceHealthServlet(int port) {
		super(port);
	}

	@Override
	public String getView() {
		return "ServiceHealth.ftl";
	}

	public String getContentType() {
		return "application/json; charset=UTF-8";
	}
}
