/**
 * Dianping.com Inc.
 * Copyright (c) 2005-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.stat.support;

public class AddressConstant {

	public static final String BALANCE_TYEP = "balance";
	public static final String ISOLATION_TYPE = "isolation";

	public static final String FAST = "fast";
	public static final String LEAST = "least";
	public static final String EXCEPTION = "exception";
	public static final String CONCURRENCY = "concurrency";

	public static final String BALACNE_FAST = BALANCE_TYEP + "_" + FAST; // "balance_fast";
	public static final String BALANCE_LEAST = BALANCE_TYEP + "_" + LEAST; // "balance_least";

	public static final String ISOLATION_EXCEPTION = ISOLATION_TYPE + "_" + EXCEPTION; // "isolation_exception";
	public static final String ISOLATION_CONCURRENCY = ISOLATION_TYPE + "_" + CONCURRENCY; // "isolation_concurrency";

}
