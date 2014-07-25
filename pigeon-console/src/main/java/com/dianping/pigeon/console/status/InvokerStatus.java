package com.dianping.pigeon.console.status;

import java.util.List;
import java.util.Map;

import com.dianping.phoenix.status.AbstractComponentStatus;
import com.dianping.pigeon.console.status.checker.InvokerStatusChecker;
import com.dianping.pigeon.console.status.checker.StatusChecker;
import com.dianping.pigeon.util.CollectionUtils;

public class InvokerStatus extends AbstractComponentStatus {

	public static final String ID = "framework.pigeon.invokers";
	private static final StatusChecker invokerStatusChecker = new InvokerStatusChecker();

	public InvokerStatus() {
		super(ID, "Pigeon Invokers");
	}

	@Override
	public State getState() {
		return null;
	}

	@Override
	public Table getData() {
		TableBuilder table = table();

		List<Map<String, Object>> info = invokerStatusChecker.collectStatusInfo();
		if (!CollectionUtils.isEmpty(info)) {
			table.header(info.get(0).keySet().toArray(new String[0]));
			for (Map<String, Object> item : info) {
				table.row(item.values());
			}
		}

		return table.build();
	}
}