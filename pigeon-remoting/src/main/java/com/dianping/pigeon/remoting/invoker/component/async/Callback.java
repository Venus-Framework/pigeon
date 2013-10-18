/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component.async;

import com.dianping.dpsf.component.DPSFRequest;
import com.dianping.dpsf.component.DPSFResponse;

public interface Callback extends Runnable, Call {

	void callback(DPSFResponse response);

	void setRequest(DPSFRequest request);

}
