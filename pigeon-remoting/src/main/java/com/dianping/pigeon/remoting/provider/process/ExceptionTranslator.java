package com.dianping.pigeon.remoting.provider.process;

import com.dianping.dpsf.exception.DPSFException;
import com.dianping.pigeon.remoting.common.exception.RpcException;

public interface ExceptionTranslator {

	DPSFException translate(RpcException ex);
}
