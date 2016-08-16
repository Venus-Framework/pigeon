/**
 *
 */
package com.dianping.pigeon.console.servlet.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.pigeon.monitor.MonitorConstants;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.console.Utils;
import com.dianping.pigeon.console.domain.ResponseError;
import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.common.exception.SecurityException;
import com.dianping.pigeon.remoting.invoker.config.spring.ReferenceBean;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.process.filter.SecurityFilter;
import com.dianping.pigeon.util.ContextUtils;

/**
 * @author sean.wang
 * @since Jul 22, 2012
 */
public class InvokeJsonServlet extends ServiceServlet {

	public InvokeJsonServlet(ServerConfig serverConfig, int port) {
		super(serverConfig, port);
	}

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	JacksonSerializer jacksonSerializer = new JacksonSerializer();

	private static final long serialVersionUID = -4886018160888366456L;

	private static Map<String, Class<?>> builtInMap = new HashMap<String, Class<?>>();

	private static final String KEY_TOKEN_ENABLE = "pigeon.provider.token.enable";

	private static final String KEY_CONSOLE_INVOKE_ENABLE = "pigeon.console.invoke.enable";

	private static final String KEY_CONSOLE_INVOKE_LOG = "pigeon.console.invoke.log";

	private static final String KEY_CONSOLE_TOKEN_HEADER = "pigeon.console.token.header";

	static {
		builtInMap.put("int", Integer.TYPE);
		builtInMap.put("long", Long.TYPE);
		builtInMap.put("double", Double.TYPE);
		builtInMap.put("float", Float.TYPE);
		builtInMap.put("boolean", Boolean.TYPE);
		builtInMap.put("char", Character.TYPE);
		builtInMap.put("byte", Byte.TYPE);
		builtInMap.put("void", Void.TYPE);
		builtInMap.put("short", Short.TYPE);
		configManager.getBooleanValue(KEY_CONSOLE_INVOKE_LOG, true);
		configManager.getBooleanValue(KEY_CONSOLE_INVOKE_ENABLE, true);
		configManager.getBooleanValue(KEY_CONSOLE_TOKEN_HEADER, false);
	}

	public boolean needValidate(HttpServletRequest request) {
		boolean needValidate = false;
		String validate = request.getParameter("validate");
		if (("true".equalsIgnoreCase(validate)) && isValidate) {
			needValidate = true;
		}
		return needValidate;
	}

	public String getContentType() {
		return "application/json; charset=UTF-8";
	}

	protected void generateView(HttpServletRequest request, HttpServletResponse response) throws IOException,
			ServletException {
		if (!configManager.getBooleanValue(KEY_CONSOLE_INVOKE_ENABLE, true)) {
			response.getWriter().write("{\"msg\":\"pigeon console invocation is disabled!\"}");
			return;
		}
		boolean direct = directInvoke;
		if (StringUtils.isNotBlank(request.getParameter("direct"))) {
			direct = request.getParameter("direct").equals("true") ? true : false;
		}
		String timeoutKey = request.getParameter("timeout");
		int timeout = timeoutKey == null ? 15 * 1000 : Integer.parseInt(timeoutKey);
		String methodName = request.getParameter("method");
		String[] types = request.getParameterValues("parameterTypes");
		if (types == null) { // for jquery ajax
			types = request.getParameterValues("parameterTypes[]");
		}
		if (types != null && "".equals(types[0])) {
			types = null;
		}
		String[] values = request.getParameterValues("parameters");
		if (values == null) { // for jquery ajax
			values = request.getParameterValues("parameters[]");
		}
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null && values[i].equals("null")) {
					values[i] = null;
				}
			}
		}
		boolean needValidate =needValidate(request);
		String token = request.getParameter("token");
		String serviceName = request.getParameter("url");
		String expectToken = null;
		String fromIp = Utils.getIpAddr(request);
		if (configManager.getBooleanValue(KEY_TOKEN_ENABLE, false)) {
			// expectToken = serviceToken;
			needValidate = true;
			String timestamp = null;
			String app = null;
			String authToken = null;
			if (configManager.getBooleanValue(KEY_CONSOLE_TOKEN_HEADER, false)) {
				timestamp = request.getHeader("Timestamp");
				String auth = request.getHeader("Authorization");
				if (StringUtils.isNotBlank(auth) && auth.startsWith("pigeon=")) {
					int idx = auth.indexOf(":");
					app = auth.substring(7, idx);
					authToken = auth.substring(idx + 1);
				}
			} else {
				timestamp = request.getParameter("timestamp");
				app = request.getParameter("app");
				ContextUtils.putRequestContext("RequestApp", app);
				authToken = request.getParameter("token");
			}
			try {
				SecurityFilter.authenticateRequest(app, fromIp, timestamp, "", authToken, serviceName, methodName);
			} catch (SecurityException e) {
				writeResponse(response, new ResponseError(e.getMessage(), null, 403));
				return;
			}
		} else if (needValidate) {
			expectToken = ServiceServlet.getToken();
			if (token == null || !token.equals(expectToken)) {
				writeResponse(response, new ResponseError("Invalid authentication code", null, 403));
				return;
			}
		}

		if (configManager.getBooleanValue(KEY_CONSOLE_INVOKE_LOG, true)) {
			logger.info("pigeon console: invoking '" + serviceName + "@" + methodName + "', from " + fromIp);
		}

		Object result = null;

		if (direct) {
			try {
				result = directInvoke(serviceName, methodName, types, values);
			} catch (InvocationTargetException e) {
				logger.error("console invoke error", e);
				if (e.getTargetException() != null) {
					result = new ResponseError("Error with service invocation", e.getTargetException(), 500);
				} else {
					result = new ResponseError(e.toString(), null, 400);
				}
			} catch (Throwable e) {
				logger.error("console invoke error", e);
				result = new ResponseError(e.toString(), null, 400);
			}
		} else {
			ContextUtils.putRequestContext("RequestIp", fromIp);
			try {
				result = proxyInvoke(serviceName, methodName, types, values, timeout);
			} catch (InvocationTargetException e) {
				logger.error("console invoke error", e);
				if (e.getTargetException() != null) {
					result = new ResponseError("Error with service invocation", e.getTargetException(), 500);
				} else {
					result = new ResponseError(e.toString(), null, 400);
				}
			} catch (Throwable e) {
				result = new ResponseError(e.toString(), null, 400);
			}
		}
		String currentMessageId = (String) ContextUtils.getLocalContext(MonitorConstants.CURRENT_MSG_ID);
		if (currentMessageId != null) {
			Map localContext = ContextUtils.getLocalContext();
			if (localContext != null && localContext.containsKey(MonitorConstants.CURRENT_MSG_ID)) {
				response.addHeader(MonitorConstants.CURRENT_MSG_ID, (String) localContext.remove(MonitorConstants.CURRENT_MSG_ID));
			}
		}
		writeResponse(response, result);
	}

	private void writeResponse(HttpServletResponse response, Object result) throws IOException {
		if (result == null) {
			return;
		}
		String json = jacksonSerializer.serializeObject(result);
		response.setContentType(getContentType());
		if (result instanceof ResponseError) {
			response.setStatus(((ResponseError) result).getStatus());
		}
		response.getWriter().write(json);
	}

	private Object proxyInvoke(String serviceName, String methodName, String[] types, String[] values, int timeout)
			throws Exception {
		ProviderConfig<?> service = getServiceProviders().get(serviceName);
		if (service == null) {
			return null;
		}
		// if (!registered) {
		// ClientManager.getInstance().registerServiceInvokers(serviceName,
		// configManager.getGroup(),
		// "console:" + serverConfig.getActualPort());
		// }
		ReferenceBean bean = new ReferenceBean();
		bean.setUrl(serviceName);
		bean.setInterfaceName(service.getServiceInterface().getName());
		bean.setVip("console:" + service.getServerConfig().getActualPort());
		bean.setTimeout(timeout);
		bean.init();
		Object proxy = bean.getObject();

		return invoke(serviceName, methodName, types, values, proxy);
	}

	private Object directInvoke(String serviceName, String methodName, String[] types, String[] values)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		ProviderConfig<?> providerConfig = getServiceProviders().get(serviceName);
		if (providerConfig == null || providerConfig.getService() == null) {
			return null;
		}

		return invoke(serviceName, methodName, types, values, providerConfig.getService());
	}

	private Object invoke(String url, String methodName, String[] types, String[] values, Object service)
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class<?> serviceClz = service.getClass();
		Class<?>[] typesClz = null;
		if (types != null) {
			typesClz = new Class<?>[types.length];
			for (int i = 0; i < types.length; i++) {
				String className = types[i];
				if ("".equals(className)) {
					// skip
				} else if (builtInMap.containsKey(className)) {
					typesClz[i] = builtInMap.get(className);
				} else {
					typesClz[i] = Class.forName(className);
				}
			}
		}
		Method method = serviceClz.getMethod(methodName, typesClz);
		method.setAccessible(true);

		return method.invoke(service, formParameters(typesClz, values));
	}

	private Object[] formParameters(Class<?>[] types, String[] values) throws SerializationException,
			ClassNotFoundException {
		if (types == null || types.length == 0) {
			return new Object[0];
		}
		Object[] valueObjs = new Object[types.length];
		;
		if (values == null) {
			valueObjs = new Object[0];
		} else {
			for (int i = 0; i < values.length; i++) {
				valueObjs[i] = jacksonSerializer.toObject(types[i], values[i]);
			}
		}
		return valueObjs;
	}

	public static Map<String, Class<?>> getBuiltInMap() {
		return builtInMap;
	}

	public static void setBuiltInMap(Map<String, Class<?>> builtInMap) {
		InvokeJsonServlet.builtInMap = builtInMap;
	}

}
