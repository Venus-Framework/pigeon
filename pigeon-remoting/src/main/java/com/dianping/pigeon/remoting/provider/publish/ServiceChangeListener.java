/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.publish;

import com.dianping.pigeon.remoting.provider.config.ProviderConfig;

public interface ServiceChangeListener {

	void notifyServicePublished(ProviderConfig<?> providerConfig);

	void notifyServiceUnpublished(ProviderConfig<?> providerConfig);

	void notifyServiceOnline(ProviderConfig<?> providerConfig);

	void notifyServiceOffline(ProviderConfig<?> providerConfig);

	void notifyServiceAdded(ProviderConfig<?> providerConfig);

	void notifyServiceRemoved(ProviderConfig<?> providerConfig);
}
