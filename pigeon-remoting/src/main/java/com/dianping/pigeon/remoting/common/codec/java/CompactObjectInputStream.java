/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.java;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

import com.dianping.pigeon.util.ClassUtils;

public class CompactObjectInputStream extends ObjectInputStream {

	private final ClassLoader classLoader;

	CompactObjectInputStream(InputStream in) throws IOException {
		this(in, null);
	}

	CompactObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
		super(in);
		this.classLoader = classLoader;
	}

	@Override
	protected void readStreamHeader() throws IOException, StreamCorruptedException {
		int version = readByte() & 0xFF;
		if (version != STREAM_VERSION) {
			throw new StreamCorruptedException("Unsupported version: " + version);
		}
	}

	@Override
	protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
		int type = read();
		if (type < 0) {
			throw new EOFException();
		}
		switch (type) {
		case CompactObjectOutputStream.TYPE_FAT_DESCRIPTOR:
			return super.readClassDescriptor();
		case CompactObjectOutputStream.TYPE_THIN_DESCRIPTOR:
			String className = readUTF();
			Class<?> clazz = loadClass(className);
			return ObjectStreamClass.lookup(clazz);
		default:
			throw new StreamCorruptedException("Unexpected class descriptor type: " + type);
		}
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		String className = desc.getName();
		try {
			return loadClass(className);
		} catch (ClassNotFoundException ex) {
			return super.resolveClass(desc);
		}
	}

	protected Class<?> loadClass(String className) throws ClassNotFoundException {
		return ClassUtils.loadClass(classLoader, className);
	}

}
