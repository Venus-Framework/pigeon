/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.serialize;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang.SerializationException;

public interface Serializer {

	Object deserialize(InputStream is) throws SerializationException;

	void serialize(OutputStream os, Object obj) throws SerializationException;

}
