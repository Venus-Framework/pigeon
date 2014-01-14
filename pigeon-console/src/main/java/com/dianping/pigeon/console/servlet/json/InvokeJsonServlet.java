/**
 *
 */
package com.dianping.pigeon.console.servlet.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;

/**
 * @author sean.wang
 * @since Jul 22, 2012
 */
public class InvokeJsonServlet extends ServiceServlet {

	public InvokeJsonServlet(int port) {
		super(port);
	}

	JacksonSerializer jacksonSerializer = new JacksonSerializer();

	private static final long serialVersionUID = -4886018160888366456L;

	private static Map<String, Class<?>> builtInMap = new HashMap<String, Class<?>>();

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
	}

	protected void generateView(HttpServletRequest request, HttpServletResponse response) throws IOException,
			ServletException {
		boolean direct = request.getParameter("direct") == null ? true : true;
		String timeoutKey = request.getParameter("timeout");
		int timeout = timeoutKey == null ? 10 * 1000 : Integer.parseInt(timeoutKey);
		String serviceName = request.getParameter("url");
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
		if (values != null && "".equals(values[0])) {
			values = null;
		}
		Object result = null;
		if (direct) {
			try {
				result = directInvoke(serviceName, methodName, types, values);
			} catch (Exception e) {
				result = e.toString();
			}
		} else {
			try {
				result = proxyInvoke(serviceName, methodName, types, values, timeout);
			} catch (InvocationTargetException e) {
				result = e.getTargetException().toString() + ":"
						+ Arrays.toString(e.getTargetException().getStackTrace());
			} catch (Exception e) {
				result = e.toString();
			}
		}
		if (result == null) {
			return;
		}
		String json = jacksonSerializer.serializeObject(result);
		response.getWriter().write(json);
	}

	private Object proxyInvoke(String serviceName, String methodName, String[] types, String[] values, int timeout)
			throws Exception {
		ProviderConfig<?> service = getServices().get(serviceName);
		if (service == null) {
			return null;
		}
		ProxyBeanFactory beanFactory = new ProxyBeanFactory();
		beanFactory.setServiceName(serviceName);
		beanFactory.setIface(service.getServiceInterface().getName());
		beanFactory.setVip("localhost:" + this.port);
		beanFactory.setTimeout(timeout);
		beanFactory.init();
		Object proxy = beanFactory.getObject();

		return invoke(methodName, types, values, proxy);
	}

	private Object directInvoke(String serviceName, String methodName, String[] types, String[] values)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		ProviderConfig<?> providerConfig = getServices().get(serviceName);
		if (providerConfig == null || providerConfig.getService() == null) {
			return null;
		}

		return invoke(methodName, types, values, providerConfig.getService());
	}

	private Object invoke(String methodName, String[] types, String[] values, Object service)
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
		// Object[] valuesObj = null;
		// if (values != null) {
		// valuesObj = new Object[values.length];
		// for (int i = 0; i < values.length; i++) {
		// valuesObj[i] = gson.fromJson(values[i], typesClz[i]);
		// }
		// }

		return method.invoke(service, formParameters(typesClz, values));
	}

	private Object[] formParameters(Class[] types, String[] values) {
		if (types == null || types.length == 0) {
			return new Object[0];
		}
		Object[] valueObjs = new Object[types.length];
		;
		if (values == null) {
			valueObjs = new Object[0];
		} else {
			for (int i = 0; i < values.length; i++) {
				valueObjs[i] = toObject(types[i], values[i]);
			}
		}
		return valueObjs;
	}

	private Object toObject(Class<?> type, String value) {
		String value_;
		if (value == null || value.length() == 0) {
			value_ = "0";
		} else {
			value_ = value;
		}
		Object valueObj = value_;
		if (type == int.class || type == Integer.class) {
			valueObj = Integer.parseInt(value_);
		} else if (type == short.class || type == Short.class) {
			valueObj = Short.parseShort(value_);
		} else if (type == byte.class || type == Byte.class) {
			valueObj = Byte.parseByte(value_);
		} else if (type == char.class) {
			valueObj = value_;
		} else if (type == long.class || type == Long.class) {
			valueObj = Long.parseLong(value_);
		} else if (type == float.class || type == Float.class) {
			valueObj = Float.parseFloat(value_);
		} else if (type == double.class || type == Double.class) {
			valueObj = Double.parseDouble(value_);
		} else if (type == String.class) {
			valueObj = String.valueOf(value);
		} else {
			if (value == null || value.length() == 0) {
				valueObj = null;
			} else {
				valueObj = jacksonSerializer.deserializeObject(type, value);
			}
		}
		return valueObj;
	}

	public static Map<String, Class<?>> getBuiltInMap() {
		return builtInMap;
	}

	public static void setBuiltInMap(Map<String, Class<?>> builtInMap) {
		InvokeJsonServlet.builtInMap = builtInMap;
	}

}
