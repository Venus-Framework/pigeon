package com.dianping.pigeon.remoting.common.status;

public enum Phase {

	TOPUBLISH("topublish"), PUBLISHING("publishing"), PUBLISHED("published"), WARMINGUP("warmingup"), WARMEDUP(
			"warmedup"), TOUNPUBLISH("tounpublish"), UNPUBLISHED("unpublished"), OFFLINE("offline"), ONLINE("online"), INVOKER_READY(
			"invoker-ready");

	private String phase;

	private Phase(String phase) {
		this.phase = phase;
	}

	public String toString() {
		return phase;
	}
}
