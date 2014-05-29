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

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.console.Utils;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

public class ServiceOnlineServlet extends HttpServlet {

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static final int WEIGHT_DEFAULT = configManager.getIntValue(Constants.KEY_WEIGHT_DEFAULT,
			Constants.DEFAULT_WEIGHT_DEFAULT);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceOnlineServlet(ServerConfig serverConfig, int port) {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ip = Utils.getIpAddr(request);
		logger.info("online all services, from " + ip);
		if (Utils.isGranted(request)) {
			try {
				ServiceFactory.setServerWeight(WEIGHT_DEFAULT);
				response.getWriter().println("ok");
			} catch (Exception e) {
				logger.error("Error with online all services", e);
				response.getWriter().println("error:" + e.getMessage());
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
