/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.domain;

public class ServiceId {

	private String url;
	private String method;

	public ServiceId(String url, String method) {
		this.url = url;
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
	public boolean equals(ServiceId an) {
		if (this.url.equals(an.getUrl()) && this.method.equals(an.getMethod())) {
			return true;
		}
		return false;
	}

	public String toString() {
		return url + "#" + method;
	}

}
