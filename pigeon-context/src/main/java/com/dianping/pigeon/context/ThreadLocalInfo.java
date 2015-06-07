/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.context;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalInfo {

	private Map<String, String> props = new HashMap<String, String>();

	public Map<String, String> getProps() {
		return props;
	}

	public void setProps(Map<String, String> props) {
		this.props = props;
	}

}
