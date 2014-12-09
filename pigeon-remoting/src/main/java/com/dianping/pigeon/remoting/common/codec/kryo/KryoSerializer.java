package com.dianping.pigeon.remoting.common.codec.kryo;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.dianping.pigeon.remoting.common.codec.DefaultAbstractSerializer;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.BigDecimalSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.BigIntegerSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.DateSerializer;

public class KryoSerializer extends DefaultAbstractSerializer {

	private Kryo kryo;

	public KryoSerializer() {
		this.kryo = new Kryo();
		kryo.register(BigDecimal.class, new BigDecimalSerializer());
		kryo.register(BigInteger.class, new BigIntegerSerializer());
		kryo.register(Date.class, new DateSerializer());
		kryo.setRegistrationRequired(false);
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
