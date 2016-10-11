package com.dianping.pigeon.remoting.common.codec.protostuff;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.dianping.pigeon.remoting.common.codec.AbstractSerializer;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffSerializer extends AbstractSerializer {

	private static ConcurrentHashMap<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();
	private static Objenesis objenesis = new ObjenesisStd(true);

	public ProtostuffSerializer() {
	}

	private static <T> Schema<T> getSchema(Class<T> cls) {
		Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
		if (schema == null) {
			schema = RuntimeSchema.createFrom(cls);
			if (schema != null) {
				cachedSchema.putIfAbsent(cls, schema);
			}
		}
		return schema;
	}

	public Object deserializeObject(InputStream is, Class<?> type) throws SerializationException {
		try {
			Object message = objenesis.newInstance(type);
			Schema schema = getSchema(type);
			ProtostuffIOUtil.mergeFrom(is, message, schema);
			return message;
		} catch (Throwable e) {
			throw new SerializationException(e.getMessage(), e);
		}
	}

	@Override
	public Object deserializeRequest(InputStream is) throws SerializationException {
		return deserializeObject(is, InvocationUtils.getRequestClass());
	}

	@Override
	public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
		LinkedBuffer buffer = LinkedBuffer.allocate(1024);
		try {
			Schema schema = getSchema(obj.getClass());
			os.write(ProtostuffIOUtil.toByteArray(obj, schema, buffer));
		} catch (Throwable e) {
			throw new SerializationException(e.getMessage(), e);
		} finally {
			buffer.clear();
		}
	}

	@Override
	public Object deserializeResponse(InputStream is) throws SerializationException {
		return deserializeObject(is, InvocationUtils.getResponseClass());
	}

	@Override
	public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
		serializeRequest(os, obj);
	}

}
