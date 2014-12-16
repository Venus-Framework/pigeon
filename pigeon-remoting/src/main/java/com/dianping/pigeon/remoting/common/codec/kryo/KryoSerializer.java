package com.dianping.pigeon.remoting.common.codec.kryo;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import com.dianping.avatar.tracker.CacheExecutionTrace;
import com.dianping.avatar.tracker.SqlExecutionTrace;
import com.dianping.avatar.tracker.TrackerContext;
import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.remoting.common.codec.DefaultAbstractSerializer;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

public class KryoSerializer extends DefaultAbstractSerializer {

	private static KryoPool pool = null;

	public KryoSerializer() {
		KryoFactory factory = new KryoFactory() {
			public Kryo create() {
				Kryo kryo = new Kryo();
				setKryo(kryo);
				// configure kryo instance, customize settings
				return kryo;
			}
		};
		pool = new KryoPool.Builder(factory).softReferences().build();
	}

	private Kryo setKryo(Kryo kryo) {
		kryo.setRegistrationRequired(false);
		kryo.setReferences(true);

		kryo.register(DefaultRequest.class, kryo.getNextRegistrationId());
		kryo.register(DefaultResponse.class, kryo.getNextRegistrationId());
		kryo.register(CacheExecutionTrace.class, kryo.getNextRegistrationId());
		kryo.register(TrackerContext.class, kryo.getNextRegistrationId());
		kryo.register(SqlExecutionTrace.class, kryo.getNextRegistrationId());

		kryo.register(HashMap.class, kryo.getNextRegistrationId());
		kryo.register(LinkedHashMap.class, kryo.getNextRegistrationId());
		kryo.register(Hashtable.class, kryo.getNextRegistrationId());
		kryo.register(ArrayList.class, kryo.getNextRegistrationId());
		kryo.register(LinkedList.class, kryo.getNextRegistrationId());
		kryo.register(HashSet.class, kryo.getNextRegistrationId());
		kryo.register(TreeSet.class, kryo.getNextRegistrationId());
		kryo.register(byte[].class, kryo.getNextRegistrationId());
		kryo.register(char[].class, kryo.getNextRegistrationId());
		kryo.register(short[].class, kryo.getNextRegistrationId());
		kryo.register(int[].class, kryo.getNextRegistrationId());
		kryo.register(long[].class, kryo.getNextRegistrationId());
		kryo.register(float[].class, kryo.getNextRegistrationId());
		kryo.register(double[].class, kryo.getNextRegistrationId());
		kryo.register(boolean[].class, kryo.getNextRegistrationId());
		kryo.register(String[].class, kryo.getNextRegistrationId());
		kryo.register(Object[].class, kryo.getNextRegistrationId());
		kryo.register(KryoSerializable.class, kryo.getNextRegistrationId());
		kryo.register(BigInteger.class, kryo.getNextRegistrationId());
		kryo.register(BigDecimal.class, kryo.getNextRegistrationId());
		kryo.register(Class.class, kryo.getNextRegistrationId());
		kryo.register(Date.class, kryo.getNextRegistrationId());
		// kryo.register(Enum.class, kryo.getNextRegistrationId());
		kryo.register(EnumSet.class, kryo.getNextRegistrationId());
		kryo.register(Currency.class, kryo.getNextRegistrationId());
		kryo.register(StringBuffer.class, kryo.getNextRegistrationId());
		kryo.register(StringBuilder.class, kryo.getNextRegistrationId());
		kryo.register(Collections.EMPTY_LIST.getClass(), kryo.getNextRegistrationId());
		kryo.register(Collections.EMPTY_MAP.getClass(), kryo.getNextRegistrationId());
		kryo.register(Collections.EMPTY_SET.getClass(), kryo.getNextRegistrationId());
		kryo.register(Collections.singletonList(null).getClass(), kryo.getNextRegistrationId());
		kryo.register(Collections.singletonMap(null, null).getClass(), kryo.getNextRegistrationId());
		kryo.register(Collections.singleton(null).getClass(), kryo.getNextRegistrationId());
		kryo.register(TreeSet.class, kryo.getNextRegistrationId());
		kryo.register(Collection.class, kryo.getNextRegistrationId());
		kryo.register(TreeMap.class, kryo.getNextRegistrationId());
		kryo.register(Map.class, kryo.getNextRegistrationId());
		kryo.register(TimeZone.class, kryo.getNextRegistrationId());
		kryo.register(Calendar.class, kryo.getNextRegistrationId());
		kryo.register(Locale.class, kryo.getNextRegistrationId());

		return kryo;
	}

	public static void registerClass(Class<?> type, int id) {
	}

	@Override
	public Object deserializeRequest(InputStream is) throws SerializationException {
		Input in = new Input(is);
		Kryo kryo = pool.borrow();
		try {
			return kryo.readClassAndObject(new Input(is));
		} catch (Throwable t) {
			throw new SerializationException(t);
		} finally {
			pool.release(kryo);
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
		Kryo kryo = pool.borrow();
		try {
			kryo.writeClassAndObject(output, obj);
		} catch (Throwable t) {
			throw new SerializationException(t);
		} finally {
			pool.release(kryo);
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
