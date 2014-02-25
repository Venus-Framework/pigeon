/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.console.listener;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.listener.ServiceChangeListener;

public class DefaultServiceChangeListener implements ServiceChangeListener {

	private static final Logger logger = LoggerLoader.getLogger(DefaultServiceChangeListener.class);

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static Set<String> publishedUrls = new HashSet<String>();

	private HttpClient getHttpClient() {
		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxTotalConnections(300);
		params.setDefaultMaxConnectionsPerHost(50);
		params.setConnectionTimeout(1000);
		params.setTcpNoDelay(true);
		params.setSoTimeout(3000);
		params.setStaleCheckingEnabled(true);
		connectionManager.setParams(params);
		HttpClient httpClient = new HttpClient();
		httpClient.setHttpConnectionManager(connectionManager);

		return httpClient;
	}

	@Override
	public void notifyServicePublished(ProviderConfig<?> providerConfig) throws ServiceException {
		if (!publishedUrls.contains(providerConfig.getUrl())) {
			logger.info("start to notify service published:" + providerConfig);
			notifyServiceChange("publish", providerConfig);
			publishedUrls.add(providerConfig.getUrl());
			logger.info("succeed to notify service published:" + providerConfig);
		}
	}

	public void notifyServiceChange(String action, ProviderConfig<?> providerConfig) throws ServiceException {
		HttpClient httpClient = getHttpClient();
		String managerAddress = configManager.getStringValue(Constants.KEY_MANAGER_ADDRESS,
				Constants.DEFAULT_MANAGER_ADDRESS);
		StringBuilder url = new StringBuilder();
		url.append("http://").append(managerAddress).append("/service/").append(action);
		url.append("?env=").append(configManager.getEnv()).append("&id=3&service=");
		url.append(providerConfig.getUrl());
		url.append("&group=").append(providerConfig.getServerConfig().getGroup());
		url.append("&ip=").append(configManager.getLocalIp());
		url.append("&port=").append(providerConfig.getServerConfig().getPort());
		GetMethod getMethod = null;
		String response = null;
		try {
			getMethod = new GetMethod(url.toString());
			httpClient.executeMethod(getMethod);
			if (getMethod.getStatusCode() >= 300) {
				throw new ServiceException("Did not receive successful HTTP response: status code = "
						+ getMethod.getStatusCode() + ", status message = [" + getMethod.getStatusText() + "]");
			}
			response = getMethod.getResponseBodyAsString();
		} catch (Throwable t) {
			throw new ServiceException("error while notifying service change to url:" + url.toString(), t);
		} finally {
			if (getMethod != null) {
				getMethod.releaseConnection();
			}
		}
		boolean isSuccess = false;
		if (response != null && response.startsWith("0")) {
			isSuccess = true;
		}
		if (!isSuccess) {
			throw new ServiceException("error while notifying service change to url:" + url.toString() + ", response:"
					+ response);
		}
	}

	@Override
	public void notifyServiceUnpublished(ProviderConfig<?> providerConfig) throws ServiceException {
		if (publishedUrls.contains(providerConfig.getUrl())) {
			logger.info("start to notify service unpublished:" + providerConfig);
			try {
				notifyServiceChange("unpublish", providerConfig);
				logger.info("succeed to notify service unpublished:" + providerConfig);
			} catch (ServiceException t) {
				logger.warn(t.getMessage());
			}
			publishedUrls.remove(providerConfig.getUrl());
		}
	}

}
