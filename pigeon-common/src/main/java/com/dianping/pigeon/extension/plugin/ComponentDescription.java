/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension.plugin;

import com.dianping.pigeon.component.xmap.annotation.XNode;
import com.dianping.pigeon.component.xmap.annotation.XObject;

@XObject(value = "componentDescription")
public class ComponentDescription {

	@XNode(value = "@code")
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@XNode(value = "@plugin")
	private String plugin;

	public String getPlugin() {
		return plugin;
	}

	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}
}
