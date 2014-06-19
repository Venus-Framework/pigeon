package com.dianping.pigeon.remoting.common.codec.hessian;

import java.math.BigInteger;
import java.util.HashMap;

import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.Serializer;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.io.StringValueDeserializer;
import com.caucho.hessian.io.StringValueSerializer;

public class HessianSerializerFactory extends SerializerFactory {

	private static HashMap serializerMap = new HashMap();
	private static HashMap deserializerMap = new HashMap();

	static {
		serializerMap.put(BigInteger.class, new StringValueSerializer());
		try {
			deserializerMap.put(BigInteger.class, new StringValueDeserializer(BigInteger.class));
		} catch (Throwable e) {
		}
	}

	public Serializer getSerializer(Class cl) throws HessianProtocolException {
		Serializer serializer = (Serializer) serializerMap.get(cl);
		if (serializer != null)
			return serializer;
		return super.getSerializer(cl);
	}

	public Deserializer getDeserializer(Class cl) throws HessianProtocolException {
		Deserializer deserializer = (Deserializer) deserializerMap.get(cl);
		if (deserializer != null)
			return deserializer;
		return super.getDeserializer(cl);
	}
}
