/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension;

public interface ExtensionInstance extends Extensible {

	Object getInstance();

	ExtensionName getName();

	int getStartLevel() throws Exception;
}
