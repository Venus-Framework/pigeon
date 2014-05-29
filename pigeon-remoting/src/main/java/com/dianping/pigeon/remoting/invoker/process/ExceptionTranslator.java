package com.dianping.pigeon.remoting.invoker.process;

import com.dianping.dpsf.exception.DPSFException;
import com.dianping.pigeon.remoting.common.exception.RpcException;

public interface ExceptionTranslator {

	RpcException translate(DPSFException ex);
}
