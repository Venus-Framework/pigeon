/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.monitor.status;

import java.util.Map;

import com.dianping.pigeon.monitor.status.Status.Level;

public final class StatusUtils {

	public static Status getSummaryStatus(Map<String, Status> statuses) {
		Level level = Level.OK;
		StringBuilder msg = new StringBuilder();
		for (Map.Entry<String, Status> entry : statuses.entrySet()) {
			String key = entry.getKey();
			Status status = entry.getValue();
			Level l = status.getLevel();
			if (Level.ERROR.equals(l)) {
				level = Level.ERROR;
				if (msg.length() > 0) {
					msg.append(",");
				}
				msg.append(key);
			} else if (Level.WARN.equals(l)) {
				if (!Level.ERROR.equals(level)) {
					level = Level.WARN;
				}
				if (msg.length() > 0) {
					msg.append(",");
				}
				msg.append(key);
			}
		}
		return new Status(level, msg.toString());
	}

}