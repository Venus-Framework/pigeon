/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.service.method;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.component.QueryString;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.common.exception.ServiceException;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;

public final class ServiceMethodFactory {

	private static Map<String, ServiceMethodCache> methods = new ConcurrentHashMap<String, ServiceMethodCache>();

	private static Set<String> ingoreMethods = new HashSet<String>();

	private static ServiceFactory serviceFactory = ExtensionLoader.getExtension(ServiceFactory.class);

	static {
		Method[] objectMethodArray = Object.class.getMethods();
		for (Method method : objectMethodArray) {
			ingoreMethods.add(method.getName());
		}

		Method[] classMethodArray = Class.class.getMethods();
		for (Method method : classMethodArray) {
			ingoreMethods.add(method.getName());
		}
	}

	public static ServiceMethod getMethod(String serviceName, String methodName, String[] paramClassNames)
			throws ServiceException {
		Map<String, Object> services = serviceFactory.getAllServices();
		ServiceMethodCache serviceMethods = methods.get(serviceName);
		if (serviceMethods == null) {
			serviceMethods = methods.get(serviceName);

			if (serviceMethods == null) {
				Object service = services.get(serviceName);

				if (service == null) {
					// 处理zone和group
					String[] parts = serviceName.split(QueryString.PREFIX_REGEXP, 2);
					if (parts.length > 1) {
						QueryString qs = new QueryString(parts[1]);
						String zone = qs.getParameter("zone");
						String group = qs.getParameter("group");
						if (zone != null && group != null) {
							// TODO 先缺省zone还是先缺省group?
							service = services.get(parts[0] + QueryString.PREFIX
									+ new QueryString().addParameter("zone", zone));
							if (service == null)
								service = services.get(parts[0] + QueryString.PREFIX
										+ new QueryString().addParameter("group", group));
						}
						if (service == null) {
							service = services.get(parts[0]);
						}
					}
					if (service == null) {
						throw new ServiceException("cannot find serivce for serviceName:" + serviceName);
					}
				}
				Method[] methodArray = service.getClass().getMethods();
				serviceMethods = new ServiceMethodCache(serviceName, service);
				for (Method method : methodArray) {
					if (!ingoreMethods.contains(method.getName())) {
						method.setAccessible(true);
						serviceMethods.addMethod(method.getName(), new ServiceMethod(service, method));
					}
				}
				methods.put(serviceName, serviceMethods);
			}
		}
		return serviceMethods.getMethod(methodName, new ServiceParam(paramClassNames));
	}

}
