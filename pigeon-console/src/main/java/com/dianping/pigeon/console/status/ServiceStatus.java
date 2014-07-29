package com.dianping.pigeon.console.status;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.phoenix.status.AbstractComponentStatus;
import com.dianping.pigeon.console.status.checker.GlobalStatusChecker;
import com.dianping.pigeon.console.status.checker.ServiceStatusChecker;
import com.dianping.pigeon.console.status.checker.StatusChecker;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.util.CollectionUtils;

public class ServiceStatus extends AbstractComponentStatus {

	public static final String ID = "framework.pigeon.services";
	private static final Logger logger = LoggerLoader.getLogger(ServiceStatus.class);
	private static final StatusChecker serviceStatusChecker = new ServiceStatusChecker();

	public ServiceStatus() {
		super(ID, "Pigeon Services");
	}

	@Override
	public State getState() {
		return GlobalStatusChecker.getState();
	}

	@Override
	public Table getData() {
		TableBuilder table = table();

		List<Map<String, Object>> info = serviceStatusChecker.collectStatusInfo();
		if (!CollectionUtils.isEmpty(info)) {
			table.header(info.get(0).keySet().toArray(new String[0]));
			for (Map<String, Object> item : info) {
				table.row(item.values().toArray());
			}
		}

		return table.build();
	}
}