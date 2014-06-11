/**
 * 
 */
package com.dianping.pigeon.console.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dianping.pigeon.console.Utils;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

public class ServicePublishServlet extends HttpServlet {

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServicePublishServlet(ServerConfig serverConfig, int port) {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ip = Utils.getIpAddr(request);
		logger.info("publishing all services, from " + ip);
		if (Utils.isGranted(request)) {
			try {
				ServiceFactory.publishAllServices();
				response.getWriter().println("ok");
			} catch (Exception e) {
				logger.error("Error while publishing all services", e);
				response.getWriter().println("error:" + e.getMessage());
			}
		} else {
			logger.warn("Forbidden!");
			response.getWriter().println("forbidden");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
