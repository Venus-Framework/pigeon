/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2014 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import com.dianping.pigeon.remoting.provider.domain.ProviderContext;

public interface ProviderInterceptor {

	public void preInvoke(ProviderContext providerContext);

	public void postInvoke(ProviderContext providerContext);

}
