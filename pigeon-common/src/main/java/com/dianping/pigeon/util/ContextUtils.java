/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.dianping.avatar.tracker.TrackerContext;
import com.dianping.pigeon.log.LoggerLoader;

public final class ContextUtils {

	private ContextUtils() {
	}

	private static final Logger logger = LoggerLoader.getLogger(ContextUtils.class);

	private static final String TRAC_ORDER = "tracker_order";

	private static Method createContextMethod = null;
	private static Method setContextMethod = null;
	private static Method clearContextMethod = null;
	private static Method getContextMethod = null;
	private static Method addSuccessContextMethod = null;
	private static Method addFailedContextMethod = null;
	private static Method getTokenMethod = null;

	private static Method getExtensionMethod = null;
	private static Method getExtensionsMethod = null;
	private static Method addExtensionMethod = null;

	private static Object[] defObjs = new Object[] {};

	private static ThreadLocal<Map> localContext = new ThreadLocal<Map>();

	private static ThreadLocal<Map<String, Serializable>> globalContext = new ThreadLocal<Map<String, Serializable>>();

	private static ThreadLocal<Map<String, Serializable>> requestContext = new ThreadLocal<Map<String, Serializable>>();

	private static ThreadLocal<Map<String, Serializable>> responseContext = new ThreadLocal<Map<String, Serializable>>();

	private static boolean enableTrackerContext = false;

	static {
		try {
			Class contextHolderClass = Class.forName("com.dianping.avatar.tracker.ExecutionContextHolder");
			Class contextClass = Class.forName("com.dianping.avatar.tracker.TrackerContext");

			createContextMethod = contextHolderClass.getDeclaredMethod("createRemoteTrackerContext",
					new Class[] { String.class });
			createContextMethod.setAccessible(true);

			setContextMethod = contextHolderClass.getDeclaredMethod("setTrackerContext", new Class[] { contextClass });
			setContextMethod.setAccessible(true);

			getContextMethod = contextHolderClass.getDeclaredMethod("getTrackerContext", new Class[] {});
			getContextMethod.setAccessible(true);

			clearContextMethod = contextHolderClass.getDeclaredMethod("clearContext", new Class[] {});
			clearContextMethod.setAccessible(true);

			addSuccessContextMethod = contextHolderClass.getDeclaredMethod("addSucceedRemoteTrackerContext",
					new Class[] { contextClass });
			addSuccessContextMethod.setAccessible(true);

			addFailedContextMethod = contextHolderClass.getDeclaredMethod("addFailedRemoteTrackerContext",
					new Class[] { contextClass });
			addFailedContextMethod.setAccessible(true);

			getTokenMethod = contextClass.getDeclaredMethod("getToken", new Class[] {});
			getTokenMethod.setAccessible(true);

			getExtensionMethod = contextClass.getDeclaredMethod("getExtension", new Class[] { String.class });
			getExtensionMethod.setAccessible(true);

			getExtensionsMethod = contextClass.getDeclaredMethod("getExtension", new Class[] {});
			getExtensionsMethod.setAccessible(true);

			addExtensionMethod = contextClass.getDeclaredMethod("addExtension", new Class[] { String.class,
					Object.class });
			addExtensionMethod.setAccessible(true);

			// configManager.registerConfigChangeListener(new
			// InnerConfigChangeListener());

			enableTrackerContext = true;
		} catch (Throwable e) {
			logger.warn("failed to load tracker context", e);
		}
	}

	public static void init() {
	}

	public static Object createContext(String serviceName, String methodName, String host, int port) {
		if (enableTrackerContext) {
			StringBuilder sb = new StringBuilder();
			sb.append(serviceName).append(".").append(methodName).append("@").append(host).append(":").append(port);
			try {
				return createContextMethod.invoke(null, new Object[] { sb.toString() });
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	public static void setContext(Object context) {
		if (enableTrackerContext && context != null) {
			try {
				setContextMethod.invoke(null, new Object[] { context });
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static Object getContext() {
		if (enableTrackerContext) {
			try {
				return getContextMethod.invoke(null, defObjs);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	private static boolean isTrackRequired() {
		TrackerContext trackerContext = (TrackerContext) getContext();
		return trackerContext != null && trackerContext.isTrackRequired();
	}

	public static void addSuccessContext(Object context) {
		if (enableTrackerContext && context != null && isTrackRequired()) {
			try {
				addSuccessContextMethod.invoke(null, new Object[] { context });
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void addFailedContext(Object context) {
		if (enableTrackerContext && context != null && isTrackRequired()) {
			try {
				addFailedContextMethod.invoke(null, new Object[] { context });
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static Integer getOrder(Object context) {
		if (enableTrackerContext && context != null) {
			try {
				return (Integer) getExtensionMethod.invoke(context, new Object[] { TRAC_ORDER });
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	public static void setOrder(Object context, Integer order) {
		if (enableTrackerContext && context != null) {
			try {
				addExtensionMethod.invoke(context, new Object[] { TRAC_ORDER, order });
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void putContextValue(String key, Serializable value) {
		if (enableTrackerContext) {
			Object context = getContext();
			if (context == null) {
				context = new TrackerContext();
				ContextUtils.setContext(context);
			}
			ContextUtils.putContextValue(context, key, value);
		}
	}

	public static void putContextValue(Object context, String key, Serializable value) {
		if (enableTrackerContext && context != null) {
			try {
				addExtensionMethod.invoke(context, new Object[] { key, value });
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static <T> T getContextValue(String key) {
		return getContextValue(ContextUtils.getContext(), key);
	}

	public static <T> T getContextValue(Object context, String key) {
		if (enableTrackerContext && context != null) {
			try {
				return (T) getExtensionMethod.invoke(context, new Object[] { key });
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	public static <T> T getContextValues(Object context) {
		if (enableTrackerContext && context != null) {
			try {
				return (T) getExtensionsMethod.invoke(context, new Object[] {});
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	public static void clearContext() {
		if (enableTrackerContext) {
			try {
				clearContextMethod.invoke(null, new Object[0]);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

	}

	public static void putLocalContext(Object key, Object value) {
		Map<Object, Object> context = localContext.get();
		if (context == null) {
			context = new HashMap<Object, Object>();
			localContext.set(context);
		}
		context.put(key, value);
	}

	public static Map getLocalContext() {
		return localContext.get();
	}

	public static Object getLocalContext(Object key) {
		Map context = localContext.get();
		if (context == null) {
			return null;
		}
		return context.get(key);
	}

	public static void clearLocalContext() {
		Map context = localContext.get();
		if (context != null) {
			context.clear();
		}
		localContext.remove();
	}

	public static void putGlobalContext(String key, Serializable value) {
		Map<String, Serializable> context = globalContext.get();
		if (context == null) {
			context = new HashMap<String, Serializable>();
			globalContext.set(context);
		}
		context.put(key, value);
	}

	public static void setGlobalContext(Map<String, Serializable> context) {
		globalContext.set(context);
	}

	public static Map<String, Serializable> getGlobalContext() {
		return globalContext.get();
	}

	public static Serializable getGlobalContext(String key) {
		Map<String, Serializable> context = globalContext.get();
		if (context == null) {
			return null;
		}
		return context.get(key);
	}

	public static void clearGlobalContext() {
		Map<String, Serializable> context = globalContext.get();
		if (context != null) {
			context.clear();
		}
		globalContext.remove();
	}

	public static void initRequestContext() {
		Map<String, Serializable> context = requestContext.get();
		if (context == null) {
			context = new HashMap<String, Serializable>();
			requestContext.set(context);
		}
	}

	public static void putRequestContext(String key, Serializable value) {
		Map<String, Serializable> context = requestContext.get();
		if (context == null) {
			context = new HashMap<String, Serializable>();
			requestContext.set(context);
		}
		context.put(key, value);
	}

	public static Map<String, Serializable> getRequestContext() {
		return requestContext.get();
	}

	public static Serializable getRequestContext(String key) {
		Map<String, Serializable> context = requestContext.get();
		if (context == null) {
			return null;
		}
		return context.get(key);
	}

	public static void clearRequestContext() {
		Map<String, Serializable> context = requestContext.get();
		if (context != null) {
			context.clear();
		}
		requestContext.remove();
	}

	public static void putResponseContext(String key, Serializable value) {
		Map<String, Serializable> context = responseContext.get();
		if (context == null) {
			context = new HashMap<String, Serializable>();
			responseContext.set(context);
		}
		context.put(key, value);
	}

	public static Map<String, Serializable> getResponseContext() {
		return responseContext.get();
	}

	public static Serializable getResponseContext(String key) {
		Map<String, Serializable> context = responseContext.get();
		if (context == null) {
			return null;
		}
		return context.get(key);
	}

	public static void setResponseContext(Map<String, Serializable> context) {
		responseContext.set(context);
	}

	public static void clearResponseContext() {
		Map<String, Serializable> context = responseContext.get();
		if (context != null) {
			context.clear();
		}
		responseContext.remove();
	}
}
