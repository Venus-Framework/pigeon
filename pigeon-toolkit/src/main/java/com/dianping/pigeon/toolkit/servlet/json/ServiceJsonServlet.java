/**
 * 
 */
package com.dianping.pigeon.toolkit.servlet.json;

import com.dianping.pigeon.toolkit.servlet.ServiceServlet;

/**
 * @author sean.wang
 * @since Jul 17, 2012
 */
public class ServiceJsonServlet extends ServiceServlet {

	private static final long serialVersionUID = -3000545547453006628L;

	public ServiceJsonServlet(int port) {
		super(port);
	}

	@Override
	public String getView() {
		return "ServiceJson.ftl";
	}

	public String getContentType() {
		return "application/json; charset=UTF-8";
	}
}
