/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.config;

import com.dianping.pigeon.component.xmap.annotation.XNode;
import com.dianping.pigeon.component.xmap.annotation.XObject;

@XObject(value = "pigeonPlugin")
public class SubscriberPluginDescriptor {

	@XNode(value = "@eventCode")
	private String eventCode;

	@XNode(value = "@plugin")
	private String plugin;

	@Override
	public String toString() {
		return "desc: eventCode=" + eventCode + " plugin=" + plugin;
	}

	public String getEventCode() {
		return this.eventCode;
	}

	public String getPlugin() {
		return this.plugin;
	}
}
