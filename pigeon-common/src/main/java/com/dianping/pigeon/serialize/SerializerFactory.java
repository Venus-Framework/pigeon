/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.serialize;

import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.serialize.hessian.HessianSerializer;
import com.dianping.pigeon.serialize.java.JavaSerializer;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public final class SerializerFactory {

	// 序列化类型---》HESSIAN序列化
	public static final byte SERIALIZE_HESSIAN = 2;
	public static final byte SERIALIZE_JAVA = 3;
	public static final byte SERIALIZE_PROTOBUF = 4;
	public static final byte SERIALIZE_HESSIAN1 = 6; // hessian spec. 1.0,
														// spec
														// 2.0兼容1.0，但1.0不兼容2.0
	private final static ConcurrentHashMap<Byte, Serializer> serializers = new ConcurrentHashMap<Byte, Serializer>();

	static {
		registerSerializer(SERIALIZE_JAVA, new JavaSerializer());
		registerSerializer(SERIALIZE_HESSIAN, new HessianSerializer());
	}

	public static void registerSerializer(byte serializerType, Serializer serializer) {
		serializers.putIfAbsent(serializerType, serializer);
	}

	public static Serializer getSerializer(byte serializerType) {
		Serializer serializer = serializers.get(serializerType);
		if(serializer == null) {
			throw new RuntimeException("no serializer found for type:" + serializerType);
		} else {
			return serializer;
		}
	}

}
