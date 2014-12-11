package com.dianping.pigeon.remoting.common.codec.kryo;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.remoting.common.codec.DefaultAbstractSerializer;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.BigDecimalSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.BigIntegerSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.DateSerializer;

public class KryoSerializer extends DefaultAbstractSerializer {

	private static Kryo kryo = new Kryo();

	public KryoSerializer() {
		kryo.register(BigDecimal.class, new BigDecimalSerializer());
		kryo.register(BigInteger.class, new BigIntegerSerializer());
		kryo.register(Date.class, new DateSerializer());
		kryo.register(InvocationRequest.class, 10);
		kryo.register(InvocationResponse.class, 11);
		kryo.register(DefaultRequest.class, 12);
		kryo.register(DefaultResponse.class, 13);
		kryo.setRegistrationRequired(false);
		kryo.setReferences(true);
		kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
	}

	public static void registerClass(Class<?> type, int id) {
		kryo.register(type, id);
	}

	@Override
	public Object deserializeRequest(InputStream is) throws SerializationException {
		Input in = new Input(is);
		try {
			return kryo.readClassAndObject(new Input(is));
		} catch (Throwable t) {
			throw new SerializationException(t);
		} finally {
			try {
				in.close();
			} catch (KryoException e) {
				throw new SerializationException(e);
			}
		}
	}

	@Override
	public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
		Output output = new Output(os);
		try {
			kryo.writeClassAndObject(output, obj);
		} catch (Throwable t) {
			throw new SerializationException(t);
		} finally {
			try {
				output.close();
			} catch (KryoException e) {
				throw new SerializationException(e);
			}
		}
	}

	@Override
	public Object deserializeResponse(InputStream is) throws SerializationException {
		return deserializeRequest(is);
	}

	@Override
	public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
		serializeRequest(os, obj);
	}

}
