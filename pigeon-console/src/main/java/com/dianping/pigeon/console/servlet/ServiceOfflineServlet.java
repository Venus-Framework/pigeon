/**
 * 
 */
package com.dianping.pigeon.console.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.console.Utils;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

public class ServiceOfflineServlet extends HttpServlet {

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceOfflineServlet(ServerConfig serverConfig, int port) {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ip = Utils.getIpAddr(request);
		logger.info("offline all services, from " + ip);
		if (Utils.isGranted(request)) {
			boolean autoUnpublishEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
					Constants.KEY_AUTOUNPUBLISH_ENABLE, true);
			boolean isOffline = autoUnpublishEnable;
			String force = request.getParameter("force");
			if ("true".equalsIgnoreCase(force)) {
				isOffline = true;
			}
			if (isOffline) {
				try {
					ServiceFactory.offline();
					response.getWriter().println("ok");
				} catch (Throwable e) {
					logger.error("Error while getting offline", e);
					response.getWriter().println("error:" + e.getMessage());
				}
			} else {
				logger.warn("auto offline is disabled!");
				response.getWriter().println("ok");
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
