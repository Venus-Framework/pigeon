/**
 *
 */
package com.dianping.pigeon.console.servlet.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.console.Utils;
import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

/**
 * @author sean.wang
 * @since Jul 22, 2012
 */
public class InvokeJsonServlet extends ServiceServlet {

	public InvokeJsonServlet(ServerConfig serverConfig, int port) {
		super(serverConfig, port);
	}

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	JacksonSerializer jacksonSerializer = new JacksonSerializer();

	private static final long serialVersionUID = -4886018160888366456L;

	private static Map<String, Class<?>> builtInMap = new HashMap<String, Class<?>>();

	private static boolean enableInvoke = configManager.getBooleanValue("pigeon.console.invoke.enable", true);

	private static boolean logInvoke = configManager.getBooleanValue("pigeon.console.invoke.log", true);

	private static boolean directInvoke = configManager.getBooleanValue("pigeon.console.invoke.direct", true);

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
		if (!enableInvoke) {
			response.getWriter().write("pigeon console invocation is disabled!");
			return;
		}
		String token = request.getParameter("token");
		if (!isValidate || isValidate && token != null && token.equals(ServiceServlet.getToken())) {
			boolean direct = directInvoke;
			if (StringUtils.isNotBlank(request.getParameter("direct"))) {
				direct = request.getParameter("direct").equals("true") ? true : false;
			}
			String timeoutKey = request.getParameter("timeout");
			int timeout = timeoutKey == null ? 15 * 1000 : Integer.parseInt(timeoutKey);
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

			if (logInvoke) {
				logger.info("pigeon console: invoking '" + serviceName + "@" + methodName + "', from "
						+ Utils.getIpAddr(request));
			}

			Object result = null;
			if (direct) {
				try {
					result = directInvoke(serviceName, methodName, types, values);
				} catch (InvocationTargetException e) {
					logger.error("console invoke error", e);
					if (e.getTargetException() != null) {
						result = ExceptionUtils.getFullStackTrace(e.getTargetException());
					} else {
						result = e.toString();
					}
				} catch (Throwable e) {
					logger.error("console invoke error", e);
					result = e.toString();
				}
			} else {
				try {
					result = proxyInvoke(serviceName, methodName, types, values, timeout);
				} catch (InvocationTargetException e) {
					logger.error("console invoke error", e);
					if (e.getTargetException() != null) {
						result = ExceptionUtils.getFullStackTrace(e.getTargetException());
					} else {
						result = e.toString();
					}
				} catch (Throwable e) {
					result = e.toString();
				}
			}
			if (result == null) {
				return;
			}
			String json = jacksonSerializer.serializeObject(result);
			response.getWriter().write(json);
		} else {
			response.getWriter().write("invalid verification code!");
		}
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
		ProxyBeanFactory beanFactory = new ProxyBeanFactory();
		beanFactory.setServiceName(serviceName);
		beanFactory.setIface(service.getServiceInterface().getName());
		beanFactory.setVip("console:" + service.getServerConfig().getActualPort());
		beanFactory.setTimeout(timeout);
		beanFactory.init();
		Object proxy = beanFactory.getObject();

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
				valueObjs[i] = toObject(types[i], values[i]);
			}
		}
		return valueObjs;
	}

	private Object toObject(Class<?> type, String value) throws SerializationException, ClassNotFoundException {
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
				if (valueObj instanceof Collection) {
					Collection valueObjList = (Collection) valueObj;
					if (!valueObjList.isEmpty()) {
						Object first = valueObjList.iterator().next();
						if (first instanceof Map) {
							Map valueMap = (Map) first;
							String valueClass = (String) valueMap.get("@class");
							if (valueClass != null) {
								valueObj = jacksonSerializer.deserializeCollection(value, type,
										Class.forName(valueClass));
							}
						}
					}
				} else if (valueObj instanceof Map) {
					Map valueObjList = (Map) valueObj;
					if (!valueObjList.isEmpty()) {
						Map finalMap = new HashMap(valueObjList.size());
						valueObj = finalMap;
						String keyClass = null;
						String valueClass = null;
						try {
							for (Iterator ir = valueObjList.keySet().iterator(); ir.hasNext();) {
								Object k = ir.next();
								Object v = valueObjList.get(k);
								Object finalKey = k;
								Object finalValue = v;
								if (k instanceof String) {
									try {
										finalKey = jacksonSerializer.deserializeObject(Map.class, (String) k);
									} catch (Throwable t) {
										if (keyClass == null) {
											Map firstValueMap = jacksonSerializer.deserializeObject(Map.class,
													(String) k);
											if (firstValueMap != null) {
												keyClass = (String) firstValueMap.get("@class");
											}
										}
										if (keyClass != null) {
											finalKey = jacksonSerializer.deserializeObject(Class.forName(keyClass),
													(String) k);
										}
									}
								}
								if (v instanceof String) {
									try {
										finalValue = jacksonSerializer.deserializeObject(Map.class, (String) v);
									} catch (Throwable t) {
										if (valueClass == null) {
											Map firstValueMap = jacksonSerializer.deserializeObject(Map.class,
													(String) v);
											if (firstValueMap != null) {
												valueClass = (String) firstValueMap.get("@class");
											}
										}
										if (valueClass != null) {
											finalValue = jacksonSerializer.deserializeObject(Class.forName(valueClass),
													(String) v);
										}
									}
								}
								finalMap.put(finalKey, finalValue);
							}
						} catch (Throwable t) {
							valueObj = valueObjList;
						}
					}
				}
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
