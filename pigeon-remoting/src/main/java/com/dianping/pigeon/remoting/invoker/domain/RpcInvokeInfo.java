package com.dianping.pigeon.remoting.invoker.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;

public class RpcInvokeInfo implements Serializable {

	private static final long serialVersionUID = 3425539738406083559L;
	private String appName;
	private long duration;
	private String serviceName;
	private String addressIp;
	private InvocationRequest request;

	public InvocationRequest getRequest() {
		return request;
	}

	public void setRequest(InvocationRequest request) {
		this.request = request;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getAddressIp() {
		return addressIp;
	}

	public void setAddressIp(String addressIp) {
		this.addressIp = addressIp;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
