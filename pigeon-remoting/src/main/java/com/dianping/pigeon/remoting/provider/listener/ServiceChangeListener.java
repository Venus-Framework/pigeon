/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.listener;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;

public interface ServiceChangeListener {

	void notifyServicePublished(ProviderConfig<?> providerConfig) throws ServiceException;

	void notifyServiceUnpublished(ProviderConfig<?> providerConfig) throws ServiceException;
}
