/**
 * 
 */
package com.dianping.pigeon.remoting.invoker.config;


/**
 * @author xiangwu
 * 
 */
public class InvokerMethodConfig {

	private String name;

	private int actives = 0;

	private int timeout = 0;

	private int retries = -1;

	private String callType;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getActives() {
		return actives;
	}

	public void setActives(int actives) {
		this.actives = actives;
	}

}
