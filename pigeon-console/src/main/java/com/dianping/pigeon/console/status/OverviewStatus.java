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
		return GlobalStatusChecker.getState();
	}

	public Map<String, Object> getProperties() {
		Map<String, Object> props = GlobalStatusChecker.getGlobalStatusProperties();
		// props.put("state", getState());
		return props;
	}
}
