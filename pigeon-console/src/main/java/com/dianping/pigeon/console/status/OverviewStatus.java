package com.dianping.pigeon.console.status;

import java.util.Map;

import com.dianping.phoenix.status.AbstractComponentStatus;
import com.dianping.pigeon.console.status.checker.GlobalStatusChecker;

public class OverviewStatus extends AbstractComponentStatus {

	public static final String ID = "framework.pigeon.overview";

	public OverviewStatus() {
		super(ID, "Pigeon Overview");
	}

	@Override
	public State getState() {
		Map<String, Object> properties = GlobalStatusChecker.getGlobalStatusProperties();
		String status = (String) properties.get("status");
		if ("ok".equalsIgnoreCase(status)) {
			return State.INITIALIZED;
		} else if ("error".equalsIgnoreCase(status)) {
			return State.FAILED;
		} else {
			return State.INITIALIZING;
		}
	}

	public Map<String, Object> getProperties() {
		return GlobalStatusChecker.getGlobalStatusProperties();
	}
}
