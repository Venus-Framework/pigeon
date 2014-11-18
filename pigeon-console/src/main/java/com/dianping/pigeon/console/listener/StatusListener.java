package com.dianping.pigeon.console.listener;

import java.util.ArrayList;
import java.util.List;

import com.dianping.pigeon.console.status.StatusInfo;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.util.CollectionUtils;

public class StatusListener {

	private static List<StatusInfo> statusList = new ArrayList<StatusInfo>();

	static {
		List<StatusInfo> list = ExtensionLoader.getExtensionList(StatusInfo.class);
		if (!CollectionUtils.isEmpty(list)) {
			statusList.addAll(list);
		}
	}

	public static void registerStatistics(StatusInfo statusInfo) {
		statusList.add(statusInfo);
	}

	public static List<StatusInfo> getStatusInfoList() {
		return statusList;
	}

}
