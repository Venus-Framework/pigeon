/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.console.status.checker;

import java.util.List;
import java.util.Map;

public interface StatusChecker {

	List<Map<String, Object>> collectStatusInfo();

	String checkError();
}