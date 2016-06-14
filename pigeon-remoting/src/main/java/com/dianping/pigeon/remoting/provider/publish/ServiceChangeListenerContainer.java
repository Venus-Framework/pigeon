/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.publish;

import java.util.ArrayList;
import java.util.List;

public class ServiceChangeListenerContainer {

	private static List<ServiceChangeListener> listeners = new ArrayList<ServiceChangeListener>();

	public static void addServiceChangeListener(ServiceChangeListener listener) {
		listeners.add(listener);
	}

	public static List<ServiceChangeListener> getListeners() {
		return listeners;
	}
}
