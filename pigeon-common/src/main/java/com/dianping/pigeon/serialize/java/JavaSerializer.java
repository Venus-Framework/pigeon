/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.serialize.java;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.lang.SerializationException;

import com.dianping.pigeon.serialize.Serializer;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public class JavaSerializer implements Serializer {

	private static ClassLoader classLoader = JavaSerializer.class.getClassLoader();

	@Override
	public Object deserialize(InputStream is) throws SerializationException {
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
	public void serialize(OutputStream os, Object obj) throws SerializationException {
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
