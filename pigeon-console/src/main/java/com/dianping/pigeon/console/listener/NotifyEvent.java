package com.dianping.pigeon.console.listener;

public class NotifyEvent {

	private String notifyUrl;
	private int retries = 0;

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

}
