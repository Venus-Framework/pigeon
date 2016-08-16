/**
 * 
 */
package com.dianping.pigeon.console.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.console.Utils;
import com.dianping.pigeon.console.domain.Service;
import com.dianping.pigeon.console.domain.ServiceMethod;
import com.dianping.pigeon.console.status.checker.GlobalStatusChecker;
import com.dianping.pigeon.console.status.checker.ProviderStatusChecker;
import com.dianping.pigeon.console.status.checker.StatusChecker;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.ServiceConfigUtils;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;
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

	protected Object model;

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());

	protected static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static final StatusChecker providerStatusChecker = new ProviderStatusChecker();

	protected static boolean isValidate = configManager.getBooleanValue("pigeon.console.invoke.validation", false);

	protected static boolean directInvoke = configManager.getBooleanValue("pigeon.console.invoke.direct", false);

	protected static String token;

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

	public Map<String, ProviderConfig<?>> getServiceProviders() {
		return ServiceFactory.getAllServiceProviders();
	}

	protected boolean initServicePage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServicePage page = new ServicePage();
		Collection<Server> servers = ProviderBootStrap.getServersMap().values();
		StringBuilder ports = new StringBuilder();
		for (Server server : servers) {
			if (server.isStarted()) {
				ports.append(server.getPort()).append("/");
			}
		}
		if (ports.length() > 0) {
			ports.deleteCharAt(ports.length() - 1);
			page.setPort(ports.toString());
		}
		page.setHttpPort(this.port);
		Map<String, ProviderConfig<?>> serviceProviders = getServiceProviders();
		for (Entry<String, ProviderConfig<?>> entry : serviceProviders.entrySet()) {
			String serviceName = entry.getKey();
			ProviderConfig<?> providerConfig = entry.getValue();
			Service s = new Service();
			s.setName(serviceName);
			Class<?> beanClass = providerConfig.getService().getClass();
			int idxCglib = beanClass.getName().indexOf("$$EnhancerByCGLIB");
			if (idxCglib != -1) {
				try {
					beanClass = Class.forName(beanClass.getName().substring(0, idxCglib));
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException("Failed to export remote service class " + beanClass.getName(), e);
				}
			}
			s.setType(beanClass);
			s.setPublished(providerConfig.isPublished() + "");
			Map<String, Method> allMethods = new HashMap<String, Method>();
			Method[] methods = ServiceConfigUtils.getServiceInterface(beanClass).getMethods();
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
			if (configManager.getBooleanValue("pigeon.provider.token.enable", false)) {
				s.setToken("true");
			}
			page.addService(s);
		}
		page.setOnline("" + GlobalStatusChecker.isOnline());
		setStatus(page, serviceProviders.isEmpty());
		String direct = request.getParameter("direct");
		if (direct == null) {
			direct = directInvoke + "";
		}
		page.setDirect(direct);
		page.setEnvironment(configManager.getEnv());
		page.setGroup(configManager.getGroup());
		page.setServiceWeights(ServicePublisher.getServerWeight());

		page.setRegistry(RegistryManager.getInstance().getRegistry().getStatistics());

		page.setAppName(configManager.getAppName());
		page.setStartTime(ProviderBootStrap.getStartTime() + "");
		page.setValidate("" + isValidate);
		page.setGovernorUrl(configManager.getStringValue("pigeon.governor.address")
				+ "/services/" + configManager.getAppName());
		this.model = page;
		return true;
	}

	private void setStatus(ServicePage page, boolean isClientSide) {
		String error = providerStatusChecker.checkError();
		if (!StringUtils.isBlank(error)) {
			page.setError(error);
			page.setStatus("error");
		}
		if (isClientSide) {// client-side
			page.setPublished("none");
		} else {// server-side
			int publishedCount = 0;
			int unpublishedCount = 0;
			Map<String, ProviderConfig<?>> services = ServicePublisher.getAllServiceProviders();
			for (Entry<String, ProviderConfig<?>> entry : services.entrySet()) {
				ProviderConfig<?> providerConfig = entry.getValue();
				if (providerConfig.isPublished()) {
					publishedCount++;
				} else {
					unpublishedCount++;
				}
			}
			if (publishedCount > 0 && unpublishedCount == 0) {
				page.setPublished("true");
			} else {
				page.setPublished("false");
			}
		}
		// set status
		if (!"error".equals(page.getStatus())) {
			page.setStatus("ok");
		}
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
		boolean result = initServicePage(request, response);
		if (result) {
			try {
				temp.process(this.model, response.getWriter());
			} catch (TemplateException e) {
				throw new ServletException(e);
			}
			if (isValidate) {
				String token = RandomUtils.newRandomString(6);
				setToken(token);
				logger.warn("current verification code:" + token + ", from " + Utils.getIpAddr(request));
			}
		}
	}

}
