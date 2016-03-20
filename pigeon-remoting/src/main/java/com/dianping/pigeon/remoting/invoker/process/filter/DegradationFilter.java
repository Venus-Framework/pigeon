/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.util.InvokerHelper;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

/**
 * @author xiangwu
 * 
 */
public class DegradationFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(DegradationFilter.class);
	private static Map<String, Object> degradeMethods = new ConcurrentHashMap<String, Object>();
	private static final String KEY_DEGRADE_ENABLE = "pigeon.invoker.degrade.enable";
	private static final String KEY_DEGRADE_METHODS = "pigeon.invoker.degrade.methods";
	private static final String KEY_DEGRADE_METHOD = "pigeon.invoker.degrade.method.return.";
	private static final JacksonSerializer jacksonSerializer = new JacksonSerializer();
	private static final Object nullObj = new Object();

	static {
		ConfigManagerLoader.getConfigManager().getBooleanValue(KEY_DEGRADE_ENABLE, false);
		String degradeMethodsConfig = ConfigManagerLoader.getConfigManager().getStringValue(KEY_DEGRADE_METHODS);
		parseDegradeMethodsConfig(degradeMethodsConfig);
		ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
	}

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith(KEY_DEGRADE_METHODS)) {
				parseDegradeMethodsConfig(value);
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {

		}

		@Override
		public void onKeyRemoved(String key) {

		}

	}

	private static void parseDegradeMethodsConfig(String degradeMethodsConfig) {
		if (StringUtils.isNotBlank(degradeMethodsConfig)) {
			ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
			try {
				String[] pairArray = degradeMethodsConfig.split(",");
				for (String str : pairArray) {
					if (StringUtils.isNotBlank(str)) {
						String[] pair = str.split("=");
						if (pair != null && pair.length == 2) {
							String key = pair[1].trim();
							Object result = nullObj;
							if (StringUtils.isNotBlank(key)) {
								String config = ConfigManagerLoader.getConfigManager().getStringValue(
										KEY_DEGRADE_METHOD + key);
								if (StringUtils.isNotBlank(config)) {
									try {
										config = config.trim();
										config = "{\"@class\":\"" + DefaultResultConfig.class.getName() + "\","
												+ config.substring(1);
										DefaultResultConfig defaultResultConfig = (DefaultResultConfig) jacksonSerializer
												.toObject(DefaultResultConfig.class, config);
										String content = defaultResultConfig.getContent();
										if (StringUtils.isNotBlank(defaultResultConfig.getKeyClass())
												&& StringUtils.isNotBlank(defaultResultConfig.getValueClass())) {
											result = jacksonSerializer.deserializeMap(content,
													Class.forName(defaultResultConfig.getReturnClass()),
													Class.forName(defaultResultConfig.getKeyClass()),
													Class.forName(defaultResultConfig.getValueClass()));
										} else if (StringUtils.isNotBlank(defaultResultConfig.getComponentClass())) {
											result = jacksonSerializer.deserializeCollection(content,
													Class.forName(defaultResultConfig.getReturnClass()),
													Class.forName(defaultResultConfig.getComponentClass()));
										} else {
											result = jacksonSerializer.toObject(
													Class.forName(defaultResultConfig.getReturnClass()), content);
										}
									} catch (Throwable t) {
										logger.error("Error while parsing default result configuration for method:"
												+ key + ", value:" + config, t);
									}
								}
							}
							map.put(pair[0].trim(), result);
						}
					}
				}
				degradeMethods.clear();
				degradeMethods = map;
			} catch (RuntimeException e) {
				logger.error("error while parsing default result configuration:" + degradeMethodsConfig, e);
			}
		} else {
			degradeMethods.clear();
		}
	}

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		InvocationRequest request = invocationContext.getRequest();
		if (ConfigManagerLoader.getConfigManager().getBooleanValue(KEY_DEGRADE_ENABLE, false)) {
			String key = request.getServiceName() + "#" + request.getMethodName();
			if (degradeMethods.containsKey(key)) {
				Object defaultResult = InvokerHelper.getDefaultResult();
				if (defaultResult != null) {
					return InvokerUtils.createDefaultResponse(defaultResult);
				} else {
					Object obj = degradeMethods.get(key);
					if (obj != nullObj) {
						return InvokerUtils.createDefaultResponse(obj);
					}
					return InvokerUtils.createDefaultResponse(null);
				}
			}
		}
		return handler.handle(invocationContext);
	}

	public static class DefaultResultConfig implements Serializable {

		private static final long serialVersionUID = 1L;

		private String returnClass;
		private String componentClass;
		private String keyClass;
		private String valueClass;
		private String content;

		public DefaultResultConfig() {
		}

		public String getReturnClass() {
			return returnClass;
		}

		public void setReturnClass(String returnClass) {
			this.returnClass = returnClass;
		}

		public String getComponentClass() {
			return componentClass;
		}

		public void setComponentClass(String componentClass) {
			this.componentClass = componentClass;
		}

		public String getKeyClass() {
			return keyClass;
		}

		public void setKeyClass(String keyClass) {
			this.keyClass = keyClass;
		}

		public String getValueClass() {
			return valueClass;
		}

		public void setValueClass(String valueClass) {
			this.valueClass = valueClass;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

	}
}
