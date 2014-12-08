package com.dianping.pigeon.remoting.common.codec.kryo;

import java.awt.List;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.BigDecimalSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.BigIntegerSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.DateSerializer;
import com.esotericsoftware.kryo.util.ObjectMap;
import com.fasterxml.jackson.databind.util.ObjectBuffer;

public class KryoSerializer {

	private static final String OBJECT_BUFFER = "ObjectBuffer";

	private Kryo kryo;
	/**
	 * @Fields initialCapacity : 初始容量
	 */
	private int initialCapacity = 512;
	/**
	 * @Fields maxCapacity : 最大容量
	 */
	private int maxCapacity = 5 * 1024 * 1024;

	private List<Class<?>> registeredClass;

	public KryoSerializer() {
		this.kryo = new Kryo();
		kryo.register(BigDecimal.class, new BigDecimalSerializer());
		kryo.register(BigInteger.class, new BigIntegerSerializer());
		kryo.register(Date.class, new DateSerializer());
		kryo.setRegistrationRequired(false);
	}

	public void init() throws ClassNotFoundException {
		if (registeredClass != null) {
			for (Class<?> clazz : registeredClass) {
				register(clazz);
			}
		}
	}

	public RegisteredClass register(Class<?> type, com.esotericsoftware.kryo.Serializer serializer) {
		return kryo.register(type, serializer);
	}

	public void register(Class<?> clazz) {
		kryo.register(clazz);
	}

	public byte[] serialize(Object object) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Output output = new Output(baos);
		kryo.writeClassAndObject(output, object);
		return baos.toByteArray();
	}

	public Object deserialize(byte[] bytes) throws Exception {
		ObjectBuffer buffer = getObjectBuffer();
		return buffer.readClassAndObject(bytes);
	}

	public void setRegisteredClass(List<Class<?>> registeredClass) {
		this.registeredClass = registeredClass;
	}

	public void setRegisteredClass(Class<?>... registeredClass) {
		if (registeredClass != null) {
			this.registeredClass = Arrays.asList(registeredClass);
		}
	}

	public void setRegistrationOptional(boolean registrationOptional) {
		kryo.setRegistrationOptional(registrationOptional);
	}

	private ObjectBuffer getObjectBuffer() {
		ObjectMap context = kryo.getContext();
		ObjectBuffer buffer = (ObjectBuffer) context.get(OBJECT_BUFFER);
		//
		if (buffer == null) {
			buffer = new ObjectBuffer(kryo, initialCapacity, maxCapacity);
			context.put(OBJECT_BUFFER, buffer);
		}
		//
		return buffer;
	}

	public void setInitialCapacity(int initialCapacity) {
		this.initialCapacity = initialCapacity;
	}

	public void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}
}
