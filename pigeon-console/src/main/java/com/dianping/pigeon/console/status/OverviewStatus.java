package com.dianping.pigeon.console.status;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dianping.phoenix.status.AbstractComponentStatus;
import com.dianping.pigeon.console.status.checker.GlobalStatusChecker;
import com.dianping.pigeon.remoting.provider.service.Phase;

public class OverviewStatus extends AbstractComponentStatus {

	public static final String ID = "framework.pigeon.overview";

	public OverviewStatus() {
		super(ID, "Pigeon Overview");
	}

	@Override
	public State getState() {
		Map<String, Object> properties = GlobalStatusChecker.getGlobalStatusProperties();
		String phase = (String) properties.get("phase");
		String error = (String) properties.get("error");
		if (phase.equals(Phase.PUBLISHED.toString()) || phase.equals(Phase.WARMINGUP.toString())
				|| phase.equals(Phase.WARMEDUP.toString()) || phase.equals(Phase.ONLINE.toString())) {
			return State.INITIALIZED;
		} else if (StringUtils.isNotBlank(error)) {
			return State.FAILED;
		} else {
			return State.INITIALIZING;
		}
	}

	public Map<String, Object> getProperties() {
		return GlobalStatusChecker.getGlobalStatusProperties();
	}
}
