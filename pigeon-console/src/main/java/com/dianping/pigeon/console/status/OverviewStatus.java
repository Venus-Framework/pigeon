package com.dianping.pigeon.console.status;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;

import com.dianping.phoenix.status.AbstractComponentStatus;
import com.dianping.pigeon.console.status.checker.GlobalStatusChecker;
import com.dianping.pigeon.console.status.checker.ProviderStatusChecker;
import com.dianping.pigeon.console.status.checker.ServiceStatusChecker;
import com.dianping.pigeon.console.status.checker.StatusChecker;
import com.dianping.pigeon.remoting.common.status.Phase;
import com.dianping.pigeon.util.CollectionUtils;

public class OverviewStatus extends AbstractComponentStatus {

	public static final String ID = "framework.pigeon.overview";
	private static final StatusChecker serviceStatusChecker = new ServiceStatusChecker();
	private static final StatusChecker providerStatusChecker = new ProviderStatusChecker();

	public OverviewStatus() {
		super(ID, "Pigeon Overview");
	}

	@Override
	public State getState() {
		Map<String, Object> props = GlobalStatusChecker.getGlobalStatusProperties();
		PropertiesBuilder propsBuilder = new PropertiesBuilder();
		for (String key : props.keySet()) {
			propsBuilder.string(key, props.get(key) + "");
		}
		propsBuilder.build();
		String error = (String) props.get("error");
		if (StringUtils.isNotBlank(error)) {
			return State.FAILED;
		}
		String phase = (String) props.get("phase");

		if (!GlobalStatusChecker.isInitialized()) {
			return State.INITIALIZED;
		} else if (StringUtils.isNotBlank(phase)) {
			if (phase.equals(Phase.TOUNPUBLISH.toString()) || phase.equals(Phase.UNPUBLISHED.toString())
					|| phase.equals(Phase.OFFLINE.toString())) {
				return State.MARKED_DOWN;
			} else if (phase.equals(Phase.PUBLISHING.toString()) || phase.equals(Phase.TOPUBLISH.toString())) {
				return State.INITIALIZING;
			} else {
				if (phase.equals(Phase.INVOKER_READY.toString())) {
					Map weightMap = (Map) props.get("weight");
					if (weightMap.isEmpty()) {// client-side
						return State.INITIALIZED;
					} else {
						return State.INITIALIZING;
					}
				}
				return State.INITIALIZED;
			}
		} else {
			return State.INITIALIZED;
		}
	}

	protected void build(ServletContext ctx) {
		TableBuilder tableBuilder = newTable();

		// services provided
		List<Map<String, Object>> servicesProvided = serviceStatusChecker.collectStatusInfo();
		if (!CollectionUtils.isEmpty(servicesProvided)) {
			tableBuilder.caption("Pigeon Services");
			tableBuilder.header(servicesProvided.get(0).keySet().toArray(new String[0]));
			for (Map<String, Object> item : servicesProvided) {
				tableBuilder.row(item.values().toArray());
			}
			tableBuilder.build();
		}

		// services invoked
		List<Map<String, Object>> servicesInvoked = providerStatusChecker.collectStatusInfo();
		if (!CollectionUtils.isEmpty(servicesInvoked)) {
			tableBuilder.caption("Pigeon Invocations");
			tableBuilder.header(servicesInvoked.get(0).keySet().toArray(new String[0]));
			for (Map<String, Object> item : servicesInvoked) {
				tableBuilder.row(item.values().toArray());
			}
			tableBuilder.build();
		}
	}

}
