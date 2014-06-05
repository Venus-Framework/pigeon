/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.java;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.dianping.pigeon.remoting.common.codec.DefaultAbstractSerializer;
import com.dianping.pigeon.remoting.common.exception.SerializationException;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public class JavaSerializer extends DefaultAbstractSerializer {

	private static ClassLoader classLoader = JavaSerializer.class.getClassLoader();

	@Override
	public Object deserializeResponse(InputStream is) throws SerializationException {
		return deserializeRequest(is);
	}

	@Override
	public Object deserializeRequest(InputStream is) throws SerializationException {
		CompactObjectInputStream coin;
		try {
			coin = new CompactObjectInputStream(is, classLoader);
			try {
				return coin.readObject();
			} finally {
				coin.close();
			}
		} catch (Throwable t) {
			throw new SerializationException(t);
		}

	}

	@Override
	public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
		serializeRequest(os, obj);
	}

	@Override
	public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
		try {
			ObjectOutputStream oout = new CompactObjectOutputStream(os);
			try {
				oout.writeObject(obj);
				oout.flush();
			} finally {
				oout.close();
			}
		} catch (Throwable t) {
			throw new SerializationException(t);
		}
	}

}
