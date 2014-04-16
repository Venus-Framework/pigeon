/**
 * 
 */
package com.dianping.pigeon.console.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigConstants;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.console.Utils;
import com.dianping.pigeon.console.domain.Service;
import com.dianping.pigeon.console.domain.ServiceMethod;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;
import com.dianping.pigeon.util.RandomUtils;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author sean.wang
 * @since Jul 16, 2012
 */
public class ServiceServlet extends HttpServlet {
	private static final long serialVersionUID = -2703014417332812558L;

	private Set<String> ingoreMethods = new HashSet<String>();

	protected int port;

	protected ServerConfig serverConfig;

	private ServicePage model;

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static String token;

	{
		Method[] objectMethodArray = Object.class.getMethods();
		for (Method method : objectMethodArray) {
			ingoreMethods.add(method.getName() + ":" + Arrays.toString(method.getParameterTypes()));
		}

	}

	private final Configuration cfg = new Configuration();

	{
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		ClassTemplateLoader templateLoader = new ClassTemplateLoader(ServiceServlet.class, "/");
		cfg.setTemplateLoader(templateLoader);
	}

	public ServiceServlet(ServerConfig serverConfig, int port) {
		this.serverConfig = serverConfig;
		this.port = port;
	}

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		ServiceServlet.token = token;
	}

	public Map<String, ProviderConfig<?>> getServices() {
		return ServiceProviderFactory.getAllServices();
	}

	private void initServicePage() {
		ServicePage page = new ServicePage();
		page.setPort(this.serverConfig.getPort());
		page.setHttpPort(this.port);
		int publishedCount = 0;
		int unpublishedCount = 0;
		Map<String, ProviderConfig<?>> services = getServices();
		for (Entry<String, ProviderConfig<?>> entry : services.entrySet()) {
			String serviceName = entry.getKey();
			ProviderConfig<?> providerConfig = entry.getValue();
			Service s = new Service();
			s.setName(serviceName);
			s.setType(providerConfig.getService().getClass());
			s.setPublished(providerConfig.isPublished() + "");
			if (providerConfig.isPublished()) {
				publishedCount++;
			} else {
				unpublishedCount++;
			}
			Map<String, Method> allMethods = new HashMap<String, Method>();
			// Class<?>[] ifaces =
			// providerConfig.getService().getClass().getInterfaces();
			// for (Class<?> iface : ifaces) {
			// String facename = iface.getName();
			// if (facename.startsWith("com.dianping") ||
			// facename.startsWith("com.dp")) {
			// Method[] methods = iface.getMethods();
			// for (Method method : methods) {
			// String key = method.getName() + ":" +
			// Arrays.toString(method.getParameterTypes());
			// if (!ingoreMethods.contains(key)) {
			// allMethods.put(key, method);
			// }
			// }
			// }
			// }
			Method[] methods = providerConfig.getServiceInterface().getMethods();
			for (Method method : methods) {
				String key = method.getName() + ":" + Arrays.toString(method.getParameterTypes());
				if (!ingoreMethods.contains(key)) {
					allMethods.put(key, method);
				}
			}
			for (Entry<String, Method> methodEntry : allMethods.entrySet()) {
				Method method = methodEntry.getValue();
				s.addMethod(new ServiceMethod(method.getName(), method.getParameterTypes(), method.getReturnType()));
			}
			page.addService(s);
		}
		if (!services.isEmpty()) {
			page.setStatus("ok");
		}
		if (publishedCount > 0 && unpublishedCount == 0) {
			page.setPublished("true");
		} else if (publishedCount == 0 && unpublishedCount >= 0) {
			page.setPublished("false");
		} else {
			page.setPublished("inprocess");
		}

		List<String> invokers = new ArrayList<String>();
		List<Server> servers = ExtensionLoader.getExtensionList(Server.class);
		for (Server server : servers) {
			List<String> serverInvokers = server.getInvokerMetaInfo();
			if (serverInvokers != null) {
				invokers.addAll(serverInvokers);
			}
		}
		page.setInvokers(invokers);
		this.model = page;
		this.model.setEnv(configManager.getEnv());
	}

	public String getView() {
		return "Service.ftl";
	}

	public String getContentType() {
		return "text/html; charset=UTF-8";
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType(getContentType());
		response.setStatus(HttpServletResponse.SC_OK);

		generateView(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	protected void generateView(HttpServletRequest request, HttpServletResponse response) throws IOException,
			ServletException {
		Template temp = cfg.getTemplate(getView());
		initServicePage();
		/*
		 * if (this.model == null) { synchronized (this) { if (this.model ==
		 * null) { initServicePage(); } } }
		 */
		try {
			temp.process(this.model, response.getWriter());
		} catch (TemplateException e) {
			throw new ServletException(e);
		}
		if (ConfigConstants.ENV_PRODUCT.equalsIgnoreCase(configManager.getEnv())) {
			String token = RandomUtils.newRandomString(6);
			setToken(token);
			logger.info("current verification code:" + token + ", from " + Utils.getIpAddr(request));
		}
	}

}
