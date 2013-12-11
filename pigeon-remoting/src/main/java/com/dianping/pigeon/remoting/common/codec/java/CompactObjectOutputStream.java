/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.java;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

public class CompactObjectOutputStream extends ObjectOutputStream {

	static final int TYPE_FAT_DESCRIPTOR = 0;
	static final int TYPE_THIN_DESCRIPTOR = 1;

	CompactObjectOutputStream(OutputStream out) throws IOException {
		super(out);
	}

	@Override
	protected void writeStreamHeader() throws IOException {
		writeByte(STREAM_VERSION);
	}

	@Override
	protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
		Class<?> clazz = desc.forClass();
		if (clazz.isPrimitive() || clazz.isArray()) {
			write(TYPE_FAT_DESCRIPTOR);
			super.writeClassDescriptor(desc);
		} else {
			write(TYPE_THIN_DESCRIPTOR);
			writeUTF(desc.getName());
		}
	}

}
