/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.context;

import java.util.Map;

public class ThreadLocalUtils {

	private static ThreadLocal<ThreadLocalInfo> tl = new ThreadLocal<ThreadLocalInfo>() {
		protected ThreadLocalInfo initialValue() {
			return new ThreadLocalInfo();
		}
	};

	public static ThreadLocalInfo getThreadLocalInfo() {
		return tl.get();
	}

	public static void disableInterrupt() {
		getThreadLocalInfo().getProps().put("interrupt", "0");
	}

	public static void enableInterrupt() {
		getThreadLocalInfo().getProps().put("interrupt", "1");
	}

	public static boolean canInterrupt() {
		Map<String, String> props = getThreadLocalInfo().getProps();
		if (props != null) {
			String value = props.get("interrupt");
			if ("0".equals(value)) {
				return false;
			}
		}
		return true;
	}

	public static boolean canInterrupt(ThreadLocalInfo threadLocalInfo) {
		if (threadLocalInfo != null) {
			Map<String, String> props = threadLocalInfo.getProps();
			if (props != null) {
				String value = props.get("interrupt");
				if ("0".equals(value)) {
					return false;
				}
			}
		}
		return true;
	}
}
