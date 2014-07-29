package com.dianping.pigeon.console.status;

import java.util.List;
import java.util.Map;

import com.dianping.phoenix.status.AbstractComponentStatus;
import com.dianping.pigeon.console.status.checker.GlobalStatusChecker;
import com.dianping.pigeon.console.status.checker.ProviderStatusChecker;
import com.dianping.pigeon.console.status.checker.StatusChecker;
import com.dianping.pigeon.util.CollectionUtils;

public class ProviderStatus extends AbstractComponentStatus {

	public static final String ID = "framework.pigeon.providers";
	private static final StatusChecker providerStatusChecker = new ProviderStatusChecker();

	public ProviderStatus() {
		super(ID, "Pigeon Providers");
	}

	@Override
	public State getState() {
		return GlobalStatusChecker.getState();
	}

	@Override
	public Table getData() {
		TableBuilder table = table();

		List<Map<String, Object>> info = providerStatusChecker.collectStatusInfo();
		if (!CollectionUtils.isEmpty(info)) {
			table.header(info.get(0).keySet().toArray(new String[0]));
			for (Map<String, Object> item : info) {
				table.row(item.values().toArray());
			}
		}

		return table.build();
	}
}