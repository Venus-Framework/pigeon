/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ClassUtils;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.remoting.common.codec.fst.FstSerializer;
import com.dianping.pigeon.remoting.common.codec.hessian.Hessian1Serializer;
import com.dianping.pigeon.remoting.common.codec.hessian.HessianSerializer;
import com.dianping.pigeon.remoting.common.codec.java.JavaSerializer;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.common.codec.protostuff.ProtostuffSerializer;
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
	public static final byte SERIALIZE_PROTO = 5;
	public static final byte SERIALIZE_HESSIAN1 = 6;
	public static final byte SERIALIZE_JSON = 7;
	public static final byte SERIALIZE_FST = 8;

	public static final String HESSIAN = "hessian";
	public static final String JAVA = "java";
	public static final String HESSIAN1 = "hessian1";
	public static final String JSON = "json";
	public static final String KRYO = "kryo";
	public static final String PROTO = "proto";
	public static final String FST = "fst";

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
			registerSerializer(SERIALIZE_PROTO, new ProtostuffSerializer());
			registerSerializer(SERIALIZE_FST, new FstSerializer());

			boolean supportJackson = true;
			try {
				ClassUtils.getClass("com.fasterxml.jackson.databind.ObjectMapper");
			} catch (ClassNotFoundException e) {
				supportJackson = false;
			}
			if (supportJackson) {
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
		} else if (JSON.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_JSON;
		} else if (PROTO.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_PROTO;
		} else if (FST.equalsIgnoreCase(serialize)) {
			return SerializerFactory.SERIALIZE_FST;
		} else {
			throw new InvalidParameterException("Only hessian/java/proto/fst serialize type supported");
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
