/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension;

public interface Extensible {

	void registerExtension(Extension extension) throws Exception;

	void unregisterExtension(Extension extension) throws Exception;
}
