package com.dianping.pigeon.remoting.common.status;

public class StatusContainer {

	private static volatile Phase phase = Phase.ONLINE;

	public static void setPhase(Phase ps) {
		phase = ps;
	}

	public static Phase getPhase() {
		return phase;
	}
}
