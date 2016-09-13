/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.RejectedException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.concurrent.FutureFactory;
import com.dianping.pigeon.remoting.invoker.concurrent.InvocationCallback;
import com.dianping.pigeon.remoting.invoker.concurrent.ServiceFutureImpl;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.config.InvokerMethodConfig;
import com.dianping.pigeon.remoting.invoker.domain.DefaultInvokerContext;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.exception.RemoteInvocationException;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.invoker.exception.ServiceDegradedException;
import com.dianping.pigeon.remoting.invoker.exception.ServiceUnavailableException;
import com.dianping.pigeon.remoting.invoker.process.DegradationManager;
import com.dianping.pigeon.remoting.invoker.process.DegradationManager.DegradeActionConfig;
import com.dianping.pigeon.remoting.invoker.process.ExceptionManager;
import com.dianping.pigeon.remoting.invoker.proxy.MockProxyWrapper;
import com.dianping.pigeon.remoting.invoker.route.quality.RequestQualityManager;
import com.dianping.pigeon.remoting.invoker.util.InvokerHelper;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiangwu
 *
 */
public class DegradationFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(DegradationFilter.class);
	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static final String KEY_DEGRADE_METHODS = "pigeon.invoker.degrade.methods";
	private static final String KEY_DEGRADE_METHOD = "pigeon.invoker.degrade.method.return.";
	private static final InvocationResponse NO_RETURN_RESPONSE = InvokerUtils.createNoReturnResponse();
	private static volatile Map<String, DegradeAction> degradeMethodActions = new ConcurrentHashMap<String, DegradeAction>();
	private static final JacksonSerializer jacksonSerializer = new JacksonSerializer();
	private final static Map<String, MockProxyWrapper> mocks = Maps.newConcurrentMap();

	static {
		String degradeMethodsConfig = configManager.getStringValue(KEY_DEGRADE_METHODS);
		try {
			parseDegradeMethodsConfig(degradeMethodsConfig);
		} catch (Throwable t) {
			logger.error("Error while parsing degradation configuration:" + degradeMethodsConfig, t);
			throw new IllegalArgumentException("Error while parsing degradation configuration:" + degradeMethodsConfig,
					t);
		}
		configManager.registerConfigChangeListener(new InnerConfigChangeListener());
	}

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith(KEY_DEGRADE_METHODS)) {
				try {
					parseDegradeMethodsConfig(value);
				} catch (Throwable t) {
					logger.error("Error while parsing degradation configuration:" + value, t);
				}
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {

		}

		@Override
		public void onKeyRemoved(String key) {

		}

	}

	private static void parseDegradeMethodsConfig(String degradeMethodsConfig) throws ClassNotFoundException {
		if (StringUtils.isNotBlank(degradeMethodsConfig)) {
			ConcurrentHashMap<String, DegradeAction> map = new ConcurrentHashMap<String, DegradeAction>();
			String[] pairArray = degradeMethodsConfig.split(",");
			for (String str : pairArray) {
				if (StringUtils.isNotBlank(str)) {
					String[] pair = str.split("=");
					if (pair != null && pair.length == 2) {
						String key = pair[1].trim();
						DegradeAction degradeAction = new DegradeAction();
						if (StringUtils.isNotBlank(key)) {
							String config = configManager.getStringValue(KEY_DEGRADE_METHOD + key);

							if (StringUtils.isNotBlank(config)) {
								config = config.trim();
								config = "{\"@class\":\"" + DegradeActionConfig.class.getName() + "\","
										+ config.substring(1);

								DegradeActionConfig degradeActionConfig = (DegradeActionConfig) jacksonSerializer
										.toObject(DegradeActionConfig.class, config);

								degradeAction.setUseMockClass(degradeActionConfig.getUseMockClass());
								boolean throwEx = degradeActionConfig.getThrowException();
								degradeAction.setThrowException(throwEx);
								String content = degradeActionConfig.getContent();
								Object returnObj = null;

								if (degradeAction.isUseMockClass()) {
									// use mock class
								} else if (degradeAction.isThrowException()) {
									if (StringUtils.isNotBlank(degradeActionConfig.getReturnClass())) {
										returnObj = jacksonSerializer.toObject(
												Class.forName(degradeActionConfig.getReturnClass()), content);
										if (!(returnObj instanceof Exception)) {
											throw new IllegalArgumentException("Invalid exception class:"
													+ degradeActionConfig.getReturnClass());
										}
										degradeAction.setReturnObj(returnObj);
									}
								} else {
									if (StringUtils.isNotBlank(degradeActionConfig.getKeyClass())
											&& StringUtils.isNotBlank(degradeActionConfig.getValueClass())) {
										returnObj = jacksonSerializer.deserializeMap(content,
												Class.forName(degradeActionConfig.getReturnClass()),
												Class.forName(degradeActionConfig.getKeyClass()),
												Class.forName(degradeActionConfig.getValueClass()));
									} else if (StringUtils.isNotBlank(degradeActionConfig.getComponentClass())) {
										returnObj = jacksonSerializer.deserializeCollection(content,
												Class.forName(degradeActionConfig.getReturnClass()),
												Class.forName(degradeActionConfig.getComponentClass()));
									} else if (StringUtils.isNotBlank(degradeActionConfig.getReturnClass())) {
										returnObj = jacksonSerializer.toObject(
												Class.forName(degradeActionConfig.getReturnClass()), content);
									}
									degradeAction.setReturnObj(returnObj);
								}
							}
						}
						map.put(pair[0].trim(), degradeAction);
					}
				}
			}
			degradeMethodActions.clear();
			degradeMethodActions = map;
		} else {
			degradeMethodActions.clear();
		}
	}

	protected InvocationResponse makeDefaultResponse(InvokerContext context, Object defaultResult) {
		InvokerConfig<?> invokerConfig = context.getInvokerConfig();
		String callType = invokerConfig.getCallType();
		InvocationResponse response = null;
		int timeout = invokerConfig.getTimeout();
		Map<String, InvokerMethodConfig> methods = invokerConfig.getMethods();
		if (!CollectionUtils.isEmpty(methods)) {
			InvokerMethodConfig methodConfig = methods.get(context.getMethodName());
			if (methodConfig != null && methodConfig.getTimeout() > 0) {
				timeout = methodConfig.getTimeout();
			}
		}
		Integer timeoutThreadLocal = InvokerHelper.getTimeout();
		if (timeoutThreadLocal != null) {
			timeout = timeoutThreadLocal;
		}
		MonitorTransaction transaction = MonitorLoader.getMonitor().getCurrentCallTransaction();
		if (transaction != null) {
			transaction.addData("CurrentTimeout", timeout);
		}
		if (Constants.CALL_SYNC.equalsIgnoreCase(callType)) {
			response = InvokerUtils.createDefaultResponse(defaultResult);
		} else if (Constants.CALL_CALLBACK.equalsIgnoreCase(callType)) {
			InvocationCallback callback = invokerConfig.getCallback();
			InvocationCallback tlCallback = InvokerHelper.getCallback();
			if (tlCallback != null) {
				callback = tlCallback;
				InvokerHelper.clearCallback();
			}
			callback.onSuccess(defaultResult);
			response = NO_RETURN_RESPONSE;
		} else if (Constants.CALL_FUTURE.equalsIgnoreCase(callType)) {
			ServiceFutureImpl future = new ServiceFutureImpl(context, timeout);
			FutureFactory.setFuture(future);
			response = InvokerUtils.createFutureResponse(future);
			future.callback(InvokerUtils.createDefaultResponse(defaultResult));
			future.run();
		} else if (Constants.CALL_ONEWAY.equalsIgnoreCase(callType)) {
			response = NO_RETURN_RESPONSE;
		}
		((DefaultInvokerContext) context).setResponse(response);
		return response;
	}

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext context) throws Throwable {
		context.getTimeline().add(new TimePoint(TimePhase.D));
		if (DegradationManager.INSTANCE.needDegrade(context)) {
			return mockResponse(context);
		} else {
			boolean failed = false;
			InvocationResponse response;
			try {
				response = handler.handle(context);
				Object responseReturn = response.getReturn();
				if (responseReturn != null) {
					int messageType = response.getMessageType();
					if (messageType == Constants.MESSAGE_TYPE_EXCEPTION) {
						RpcException rpcException = InvokerUtils.toRpcException(response);
						if (rpcException instanceof RemoteInvocationException || rpcException instanceof RejectedException) {
							failed = true;
							DegradationManager.INSTANCE.addFailedRequest(context, rpcException);
						}
					}
				}

				return response;

			} catch (ServiceUnavailableException | RemoteInvocationException | RequestTimeoutException | RejectedException e) {
				failed = true;

				if (DegradationManager.INSTANCE.needFailureDegrade()) {
					return mockResponse(context);
				} else {
					DegradationManager.INSTANCE.addFailedRequest(context, e);
					throw e;
				}
			} finally {
				RequestQualityManager.INSTANCE.addClientRequest(context, failed);
			}
		}
	}

	private InvocationResponse mockResponse(InvokerContext context) throws Throwable {
		String key = DegradationManager.INSTANCE.getRequestUrl(context);
		Object defaultResult = InvokerHelper.getDefaultResult();
		DegradeAction action = degradeMethodActions.get(key);

		if (defaultResult == null && action != null) {
			try {
				defaultResult = action.getReturnObj();

				if (action.isUseMockClass()) {

					if (context.getInvokerConfig().getMock() != null) {
						defaultResult = getMockResult(context);
					} else {
						logger.warn("no mock obj defined in invoker config, return null instead!");
					}

				} else if (action.isThrowException()) {

					return throwException(key, context, action);
				}

				return makeDefaultResponse(context, defaultResult);

			} finally {
				DegradationManager.INSTANCE.addDegradedRequest(context);
			}
		} else if(defaultResult == null) {
			logger.warn("no degrade method action found, return null instead!");
		}

		try {
			return makeDefaultResponse(context, defaultResult);
		} finally {
			DegradationManager.INSTANCE.addDegradedRequest(context);
		}
	}

	private Object getMockResult(InvokerContext context) throws Throwable {
		InvokerConfig invokerConfig = context.getInvokerConfig();
		String mockService = invokerConfig.getUrl();
		MockProxyWrapper mockProxyWrapper = mocks.get(mockService);

		if (mockProxyWrapper == null) {
			mockProxyWrapper = new MockProxyWrapper(invokerConfig.getMock());
		}

		return mockProxyWrapper.invoke(context.getMethodName(),
				context.getParameterTypes(), context.getArguments());
	}

	protected InvocationResponse throwException(String key, InvokerContext context, DegradeAction action)
			throws Exception {
		Exception exception;
		if (action.getReturnObj() == null) {
			exception = new ServiceDegradedException("Degraded method:" + key);
		} else {
			exception = (Exception) action.getReturnObj();
		}

		InvokerConfig<?> invokerConfig = context.getInvokerConfig();
		String callType = invokerConfig.getCallType();
		InvocationResponse response = null;
		int timeout = invokerConfig.getTimeout();
		Map<String, InvokerMethodConfig> methods = invokerConfig.getMethods();
		if (!CollectionUtils.isEmpty(methods)) {
			InvokerMethodConfig methodConfig = methods.get(context.getMethodName());
			if (methodConfig != null && methodConfig.getTimeout() > 0) {
				timeout = methodConfig.getTimeout();
			}
		}
		Integer timeoutThreadLocal = InvokerHelper.getTimeout();
		if (timeoutThreadLocal != null) {
			timeout = timeoutThreadLocal;
		}
		MonitorTransaction transaction = MonitorLoader.getMonitor().getCurrentCallTransaction();
		if (transaction != null) {
			transaction.addData("CurrentTimeout", timeout);
		}
		if (Constants.CALL_SYNC.equalsIgnoreCase(callType)) {
			throw exception;
		} else if (Constants.CALL_CALLBACK.equalsIgnoreCase(callType)) {
			InvocationCallback callback = invokerConfig.getCallback();
			InvocationCallback tlCallback = InvokerHelper.getCallback();
			if (tlCallback != null) {
				callback = tlCallback;
				InvokerHelper.clearCallback();
			}
			callback.onFailure(exception);
			ExceptionManager.INSTANCE.logRpcException(null, invokerConfig.getUrl(), context.getMethodName(),
					"callback degraded", exception, null, response, transaction);
			response = NO_RETURN_RESPONSE;
		} else if (Constants.CALL_FUTURE.equalsIgnoreCase(callType)) {
			ServiceFutureImpl future = new ServiceFutureImpl(context, timeout);
			FutureFactory.setFuture(future);
			response = InvokerUtils.createFutureResponse(future);
			future.callback(InvokerUtils.createThrowableResponse(exception));
			future.run();
		} else if (Constants.CALL_ONEWAY.equalsIgnoreCase(callType)) {
			response = NO_RETURN_RESPONSE;
		}
		((DefaultInvokerContext) context).setResponse(response);
		return response;
	}

	private static class DegradeAction implements Serializable {
		private boolean throwException = false;
		private Object returnObj;
		private boolean useMockClass = false;

		public boolean isUseMockClass() {
			return useMockClass;
		}

		public void setUseMockClass(boolean useMockClass) {
			this.useMockClass = useMockClass;
		}

		public boolean isThrowException() {
			return throwException;
		}

		public void setThrowException(boolean throwException) {
			this.throwException = throwException;
		}

		public Object getReturnObj() {
			return returnObj;
		}

		public void setReturnObj(Object returnObj) {
			this.returnObj = returnObj;
		}

	}
}