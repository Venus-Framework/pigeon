/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension.plugin;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.component.xmap.annotation.XContent;
import com.dianping.pigeon.component.xmap.annotation.XNode;
import com.dianping.pigeon.component.xmap.annotation.XObject;

@XObject(value = "plugin")
public class Plugin {

	@XNode(value = "@phase")
	private String phase;

	@XNode(value = "@component")
	private String component;

	@XNode(value = "@name")
	private String name;

	@XNode(value = "@point")
	private String point;

	@XNode(value = "@descriptor")
	private String descriptor;

	@XContent(value = "content")
	private String content;

	private Object descriptorObject;

	public Object getDescriptorObject() {
		return descriptorObject;
	}

	public void setDescriptorObject(Object descriptorObject) {
		this.descriptorObject = descriptorObject;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String getPoint() {
		return point;
	}

	public void setPoint(String point) {
		this.point = point;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
