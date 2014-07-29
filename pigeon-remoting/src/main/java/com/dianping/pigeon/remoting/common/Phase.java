package com.dianping.pigeon.remoting.common;

public enum Phase {

	TOPUBLISH("topublish"), PUBLISHING("publishing"), PUBLISHED("published"), WARMINGUP("warmingup"), WARMEDUP(
			"warmedup"), TOUNPUBLISH("tounpublish"), UNPUBLISHED("unpublished"), OFFLINE("offline"), ONLINE("online");

	private String phase;

	private Phase(String phase) {
		this.phase = phase;
	}

	public String toString() {
		return phase;
	}
}
