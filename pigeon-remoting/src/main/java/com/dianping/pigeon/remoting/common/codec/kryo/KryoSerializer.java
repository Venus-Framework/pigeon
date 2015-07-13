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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import com.dianping.avatar.tracker.CacheExecutionTrace;
import com.dianping.avatar.tracker.SqlExecutionTrace;
import com.dianping.avatar.tracker.TrackerContext;
import com.dianping.dpsf.exception.DPSFException;
import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.codec.DefaultAbstractSerializer;
import com.dianping.pigeon.remoting.common.exception.ApplicationException;
import com.dianping.pigeon.remoting.common.exception.InvalidParameterException;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.common.exception.RejectedException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.invoker.exception.RemoteInvocationException;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.invoker.exception.ServiceUnavailableException;
import com.dianping.pigeon.remoting.provider.exception.InvocationFailureException;
import com.dianping.pigeon.remoting.provider.exception.ProcessTimeoutException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

public class KryoSerializer extends DefaultAbstractSerializer {

	private static KryoPool pool = null;
	private static Map<Class<?>, Integer> types = new LinkedHashMap<Class<?>, Integer>();
	private static final int poolSize = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.serialize.kryo.poolsize", 50);
	private static List<Kryo> kryoPool = new ArrayList<Kryo>();

	public KryoSerializer() {
		KryoFactory factory = new KryoFactory() {
			public Kryo create() {
				Kryo kryo = new Kryo();
				setKryo(kryo);
				for (Class<?> type : types.keySet()) {
					kryo.register(type, types.get(type));
				}
				kryoPool.add(kryo);
				// configure kryo instance, customize settings
				return kryo;
			}
		};
		pool = new KryoPool.Builder(factory).softReferences().build();
		try {
			for (int i = 0; i < poolSize; i++) {
				kryoPool.add(pool.borrow());
			}
		} finally {
			for (Kryo kryo : kryoPool) {
				pool.release(kryo);
			}
		}
	}

	private Kryo setKryo(Kryo kryo) {
		kryo.setRegistrationRequired(false);
		kryo.setReferences(true);

		int idx = 10;
		kryo.register(HashMap.class, idx++);
		kryo.register(LinkedHashMap.class, idx++);
		kryo.register(Hashtable.class, idx++);
		kryo.register(ArrayList.class, idx++);
		kryo.register(LinkedList.class, idx++);
		kryo.register(HashSet.class, idx++);
		kryo.register(TreeSet.class, idx++);
		kryo.register(byte[].class, idx++);
		kryo.register(char[].class, idx++);
		kryo.register(short[].class, idx++);
		kryo.register(int[].class, idx++);
		kryo.register(long[].class, idx++);
		kryo.register(float[].class, idx++);
		kryo.register(double[].class, idx++);
		kryo.register(boolean[].class, idx++);
		kryo.register(String[].class, idx++);
		kryo.register(Object[].class, idx++);
		kryo.register(KryoSerializable.class, idx++);
		kryo.register(BigInteger.class, idx++);
		kryo.register(BigDecimal.class, idx++);
		kryo.register(Class.class, idx++);
		kryo.register(Date.class, idx++);
		// kryo.register(Enum.class, idx++);
		kryo.register(EnumSet.class, idx++);
		kryo.register(Currency.class, idx++);
		kryo.register(StringBuffer.class, idx++);
		kryo.register(StringBuilder.class, idx++);
		kryo.register(Collections.EMPTY_LIST.getClass(), idx++);
		kryo.register(Collections.EMPTY_MAP.getClass(), idx++);
		kryo.register(Collections.EMPTY_SET.getClass(), idx++);
		kryo.register(Collections.singletonList(null).getClass(), idx++);
		kryo.register(Collections.singletonMap(null, null).getClass(), idx++);
		kryo.register(Collections.singleton(null).getClass(), idx++);
		kryo.register(TreeSet.class, idx++);
		kryo.register(Collection.class, idx++);
		kryo.register(TreeMap.class, idx++);
		kryo.register(Map.class, idx++);
		kryo.register(TimeZone.class, idx++);
		kryo.register(Calendar.class, idx++);
		kryo.register(Locale.class, idx++);

		idx = 9000;
		kryo.register(DefaultRequest.class, idx++);
		kryo.register(DefaultResponse.class, idx++);
		kryo.register(CacheExecutionTrace.class, idx++);
		kryo.register(TrackerContext.class, idx++);
		kryo.register(SqlExecutionTrace.class, idx++);
		
		kryo.register(DPSFException.class, idx++);
		kryo.register(RpcException.class, idx++);
		kryo.register(NetTimeoutException.class, idx++);
		kryo.register(ApplicationException.class, idx++);
		kryo.register(InvalidParameterException.class, idx++);
		kryo.register(NetworkException.class, idx++);
		kryo.register(RejectedException.class, idx++);
		kryo.register(SecurityException.class, idx++);
		kryo.register(SerializationException.class, idx++);
		kryo.register(RemoteInvocationException.class, idx++);
		kryo.register(RequestTimeoutException.class, idx++);
		kryo.register(ServiceUnavailableException.class, idx++);
		kryo.register(InvocationFailureException.class, idx++);
		kryo.register(ProcessTimeoutException.class, idx++);

		return kryo;
	}

	public static void registerClass(Class<?> type, int id) {
		if (id < 10000) {
			throw new IllegalArgumentException("register class id must be greater than 10000");
		}
		if (!types.containsKey(type)) {
			types.put(type, id);
		}
		for (Kryo kryo : kryoPool) {
			kryo.register(type, id);
		}
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
