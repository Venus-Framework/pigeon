package com.dianping.pigeon.service.core.plugin;

import com.dianping.pigeon.component.xmap.annotation.XNode;
import com.dianping.pigeon.component.xmap.annotation.XObject;

@XObject(value = "pigeonTestPlugin")
public class PigeonTestPlugin {

	@XNode(value = "@eventCode")
	private String eventCode;

	@XNode(value = "@plugin")
	private String plugin;

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	public String getPlugin() {
		return plugin;
	}

	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}
}
