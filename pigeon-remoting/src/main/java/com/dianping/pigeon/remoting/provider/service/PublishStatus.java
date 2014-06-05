package com.dianping.pigeon.remoting.provider.service;

public enum PublishStatus {

	TOPUBLISH("topublish"), PUBLISHING("publishing"), PUBLISHED("published"), WARMINGUP("warmingup"), WARMEDUP(
			"warmedup"), TOUNPUBLISH("tounpublish"), UNPUBLISHED("unpublished"), OFFLINE("offline");

	private String status;

	private PublishStatus(String status) {
		this.status = status;
	}

	public String toString() {
		return status;
	}
}
