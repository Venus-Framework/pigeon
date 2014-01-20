/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec;

import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.remoting.common.codec.hessian.HessianSerializer;
import com.dianping.pigeon.remoting.common.codec.java.JavaSerializer;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.common.codec.protobuf.ProtobufSerializer;

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
	public static final byte SERIALIZE_HESSIAN1 = 6;
	public static final byte SERIALIZE_JSON = 7;

	public static final String HESSIAN = "hessian";
	public static final String JAVA = "java";
	public static final String PROTOBUF = "protobuf";
	public static final String HESSIAN1 = "hessian";
	public static final String JSON = "json";

	private final static ConcurrentHashMap<Byte, Serializer> serializers = new ConcurrentHashMap<Byte, Serializer>();

	static {
		init();
	}

	public static void init() {
		registerSerializer(SERIALIZE_JAVA, new JavaSerializer());
		registerSerializer(SERIALIZE_HESSIAN, new HessianSerializer());
		registerSerializer(SERIALIZE_HESSIAN1, new HessianSerializer());
		if(ProtobufSerializer.support()) {
			registerSerializer(SERIALIZE_PROTOBUF, new ProtobufSerializer());
		}
		registerSerializer(SERIALIZE_JSON, new JacksonSerializer());
	}

	public static byte getSerialize(String serialize) {
		if (JAVA.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_JAVA;
		} else if (HESSIAN.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_HESSIAN;
		} else if (PROTOBUF.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_PROTOBUF;
		} else if (JSON.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_JSON;
		} else {
			throw new IllegalArgumentException("Only hessian, java, protobuf, json serialize type supported now!");
		}
	}

	public static void registerSerializer(byte serializerType, Serializer serializer) {
		if (serializer == null) {
			throw new IllegalArgumentException("the serializer is null");
		}
		serializers.putIfAbsent(serializerType, serializer);
	}

	public static Serializer getSerializer(byte serializerType) {
		Serializer serializer = serializers.get(serializerType);
		if (serializer == null) {
			throw new RuntimeException("no serializer found for type:" + serializerType);
		} else {
			return serializer;
		}
	}

}
