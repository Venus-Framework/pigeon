package com.dianping.pigeon.remoting.invoker.process;

import org.apache.commons.lang.StringUtils;

import com.dianping.dpsf.exception.DPSFException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.invoker.exception.RemoteInvocationException;

public class InvokerExceptionTranslator {

	public Throwable translate(DPSFException e) {
		String msg = e.getMessage();
		if (msg != null && msg.startsWith("#")) {
			int idx = msg.indexOf("@");
			if (idx != -1) {
				String errorCode = msg.substring(1, idx);
				if (StringUtils.isNotBlank(errorCode)) {
					// int idx2 = msg.indexOf("@", idx + 1);
					// if (idx2 != -1) {
					// msg = msg.substring(idx2);
					// } else {
					// msg = msg.substring(idx);
					// }
					return translate(errorCode, msg, e.getStackTrace());
				}
			}
		}
		return e;
	}

	private RpcException translate(String errorCode, String msg, StackTraceElement[] stackTrace) {
		RpcException e = new RemoteInvocationException(msg);
		if (e != null) {
			e.setStackTrace(stackTrace);
		}
		return e;
	}
}
