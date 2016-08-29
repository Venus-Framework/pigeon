/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.service.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.remoting.common.exception.BadRequestException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.exception.InvocationFailureException;
import com.dianping.pigeon.util.ClassUtils;

public class ServiceMethodCache {

	/**
	 * 根据方法名和参数个数Map方法集合
	 */
	private Map<String, Map<Integer, List<ServiceMethod>>> methods = new ConcurrentHashMap<String, Map<Integer, List<ServiceMethod>>>();

	private Map<String, Map<ServiceParam, ServiceMethod>> bestMacthMethod = new ConcurrentHashMap<String, Map<ServiceParam, ServiceMethod>>();

	private ServiceMethod currentMethod;

	private int methodSize = 0;

	private Object service;

	// private String serviceName;

	public ServiceMethodCache(String serviceName, Object service) {
		// this.serviceName = serviceName;
		this.service = service;
	}

	public ServiceMethod getCurrentMethod() {
		return currentMethod;
	}

	void addMethod(String methodName, ServiceMethod method) {
		if (this.currentMethod == null) {
			this.currentMethod = method;
		}
		Map<Integer, List<ServiceMethod>> methodMap = this.methods.get(methodName);
		if (methodMap == null) {
			methodMap = new HashMap<Integer, List<ServiceMethod>>();
			this.methods.put(methodName, methodMap);
		}
		List<ServiceMethod> methodList = methodMap.get(method.getParameterSize());
		if (methodList == null) {
			methodList = new ArrayList<ServiceMethod>();
			methodMap.put(method.getParameterSize(), methodList);
		}
		methodList.add(method);
		methodSize++;
	}

	public ServiceMethod getMethod(String methodName, ServiceParam paramNames) throws InvocationFailureException {
		if (methodSize == 1) {
			return this.currentMethod;
		} else {
			ServiceMethod method = getBestMatchMethodForCache(methodName, paramNames);
			if (method == null) {
				synchronized (this) {
					method = getBestMatchMethodForCache(methodName, paramNames);
					if (method == null) {
						method = getBestMatchMethod(methodName, paramNames);
						this.bestMacthMethod.get(methodName).put(paramNames, method);
					}
				}
			}
			return method;
		}
	}

	private ServiceMethod getBestMatchMethodForCache(String methodName, ServiceParam paramNames) {
		Map<ServiceParam, ServiceMethod> paramMethodMap = this.bestMacthMethod.get(methodName);
		if (paramMethodMap == null) {
			paramMethodMap = new HashMap<ServiceParam, ServiceMethod>();
			this.bestMacthMethod.put(methodName, paramMethodMap);
		}
		return paramMethodMap.get(paramNames);
	}

	private ServiceMethod getBestMatchMethod(String methodName, ServiceParam paramNames)
			throws InvocationFailureException {
		Map<Integer, List<ServiceMethod>> methodMap = this.methods.get(methodName);
		if (methodMap == null) {
			throw new BadRequestException("the service " + this.service + " is not matched with method:"
					+ methodName);
		}
		List<ServiceMethod> methodList = methodMap.get(paramNames.getLength());
		if (methodList == null || methodList.size() == 0) {
			throw new BadRequestException("the service " + this.service + " is not matched with method:"
					+ methodName + " for " + paramNames.getLength() + " parameters");
		}
		if (paramNames.getLength() == 0) {
			return methodList.get(0);
		}
		int matchingValue = -1;
		ServiceMethod bestMethod = null;
		for (ServiceMethod m : methodList) {
			int mv = matching(m, paramNames.getParamNames(), false);
			if (mv > matchingValue) {
				matchingValue = mv;
				bestMethod = m;
			}
		}
		if (matchingValue < 0) {
			for (ServiceMethod m : methodList) {
				int mv = matching(m, paramNames.getParamNames(), true);
				if (mv > matchingValue) {
					matchingValue = mv;
					bestMethod = m;
				}
			}
			if (matchingValue >= 0) {
				bestMethod.setNeedCastParameterClasses(true);
			}
		}
		if (matchingValue < 0) {
			throw new BadRequestException("the service " + this.service + " is not matched with method:"
					+ methodName + " for parameter class types");
		}
		return bestMethod;
	}

	/**
	 * 
	 * 返回匹配度 如果返回值等于参数个数，表示完全匹配 如果返回值为0---参数个数，表示部分匹配 如果返回-1，表示有不匹配项
	 * 
	 * @param paramClassNames
	 * @return
	 * @throws InvocationFailureException
	 */
	private int matching(ServiceMethod method, String[] paramClassNames, boolean cast)
			throws InvocationFailureException {
		int k = 0;
		for (int i = 0; i < paramClassNames.length; i++) {
			if (paramClassNames[i].equals(Constants.TRANSFER_NULL)) {
				continue;
			}
			Class<?> paramClass = null;
			try {
				paramClass = ClassUtils.loadClass(paramClassNames[i]);
			} catch (ClassNotFoundException e) {
				throw new BadRequestException("no class found for parameter:" + paramClassNames[i]);
			}
			if (paramClass == method.getParameterClasses()[i]) {
				k++;
			} else if (cast) {
				if (paramClassNames[i].equals(Double.class.getName())) {
					paramClass = Float.class;
				} else if (paramClassNames[i].equals(Integer.class.getName())) {
					paramClass = Short.class;
				} 
				if (paramClass == method.getParameterClasses()[i]) {
					k++;
				}
			}
			if (!method.getParameterClasses()[i].isAssignableFrom(paramClass)) {
				return -1;
			}
		}
		return k;
	}

	public Map<String, Map<Integer, List<ServiceMethod>>> getMethodMap() {
		return methods;
	}
}
