/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.codec.hessian.Hessian1Serializer;
import com.dianping.pigeon.remoting.common.codec.hessian.HessianSerializer;
import com.dianping.pigeon.remoting.common.codec.java.JavaSerializer;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.common.codec.protobuf.ProtobufSerializer;
import com.dianping.pigeon.remoting.common.exception.InvalidParameterException;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public final class SerializerFactory {

	private static final Logger logger = LoggerLoader.getLogger(SerializerFactory.class);

	// 序列化类型---》HESSIAN序列化
	public static final byte SERIALIZE_HESSIAN = 2;
	public static final byte SERIALIZE_JAVA = 3;
	public static final byte SERIALIZE_PROTOBUF = 4;
	public static final byte SERIALIZE_HESSIAN1 = 6;
	public static final byte SERIALIZE_JSON = 7;

	public static final String HESSIAN = "hessian";
	public static final String JAVA = "java";
	public static final String PROTOBUF = "protobuf";
	public static final String HESSIAN1 = "hessian1";
	public static final String JSON = "json";

	private static volatile boolean isInitialized = false;

	private final static ConcurrentHashMap<Byte, Serializer> serializers = new ConcurrentHashMap<Byte, Serializer>();

	static {
		init();
	}

	public static void init() {
		if (!isInitialized) {
			registerSerializer(SERIALIZE_JAVA, new JavaSerializer());
			registerSerializer(SERIALIZE_HESSIAN, new HessianSerializer());
			registerSerializer(SERIALIZE_HESSIAN1, new Hessian1Serializer());
			if (ProtobufSerializer.support()) {
				try {
					registerSerializer(SERIALIZE_PROTOBUF, new ProtobufSerializer());
				} catch (Throwable t) {
					logger.warn("failed to initialize protobuf serializer:" + t.getMessage());
				}
			}
			if (JacksonSerializer.support()) {
				try {
					registerSerializer(SERIALIZE_JSON, new JacksonSerializer());
				} catch (Throwable t) {
					logger.warn("failed to initialize jackson serializer:" + t.getMessage());
				}
			}
			isInitialized = true;
		}
	}

	public static byte getSerialize(String serialize) {
		if (JAVA.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_JAVA;
		} else if (HESSIAN.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_HESSIAN;
		} else if (HESSIAN1.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_HESSIAN1;
		} else if (PROTOBUF.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_PROTOBUF;
		} else if (JSON.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_JSON;
		} else {
			throw new InvalidParameterException("Only hessian, java, protobuf, json serialize type supported now!");
		}
	}

	public static void registerSerializer(byte serializerType, Serializer serializer) {
		if (serializer == null) {
			throw new InvalidParameterException("the serializer is null");
		}
		serializers.putIfAbsent(serializerType, serializer);
	}

	public static Serializer getSerializer(byte serializerType) {
		Serializer serializer = serializers.get(serializerType);
		if (serializer == null) {
			throw new InvalidParameterException("no serializer found for type:" + serializerType);
		} else {
			return serializer;
		}
	}

}
