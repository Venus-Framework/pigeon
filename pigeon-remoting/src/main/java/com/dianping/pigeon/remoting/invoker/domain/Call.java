/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.domain;

import com.dianping.pigeon.remoting.invoker.Client;

public interface Call {

	void setClient(Client client);

	Client getClient();
}
