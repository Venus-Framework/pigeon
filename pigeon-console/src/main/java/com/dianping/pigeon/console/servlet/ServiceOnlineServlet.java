/**
 * 
 */
package com.dianping.pigeon.console.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

public class ServiceOnlineServlet extends HttpServlet {

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static final List<String> LOCAL_IP_LIST = new ArrayList<String>();

	static {
		LOCAL_IP_LIST.add("127.0.0.1");
		LOCAL_IP_LIST.add("0:0:0:0:0:0:0:1");
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceOnlineServlet(ServerConfig serverConfig, int port) {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ip = Utils.getIpAddr(request);
		logger.info("publishing all services, from " + ip);
		if (LOCAL_IP_LIST.contains(ip) || ip.equals(configManager.getLocalIp())) {
			try {
				ServiceFactory.setServerWeight(1);
				response.getWriter().println("ok");
			} catch (Exception e) {
				logger.error("Error while publishing all services", e);
				response.getWriter().println("error:" + e.getMessage());
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
