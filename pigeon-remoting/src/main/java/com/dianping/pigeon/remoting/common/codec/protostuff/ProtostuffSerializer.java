package com.dianping.pigeon.remoting.common.codec.protostuff;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.remoting.common.codec.DefaultAbstractSerializer;
import com.dianping.pigeon.remoting.common.exception.SerializationException;

public class ProtostuffSerializer extends DefaultAbstractSerializer {

	private static ConcurrentHashMap<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();
	private static Objenesis objenesis = new ObjenesisStd(true);

	public ProtostuffSerializer() {
		Schema<DefaultRequest> requestSchema = RuntimeSchema.createFrom(DefaultRequest.class);
		RuntimeSchema.register(DefaultRequest.class, requestSchema);
		cachedSchema.put(DefaultRequest.class, requestSchema);

		Schema<DefaultResponse> responseSchema = RuntimeSchema.createFrom(DefaultResponse.class);
		RuntimeSchema.register(DefaultResponse.class, responseSchema);
		cachedSchema.put(DefaultResponse.class, responseSchema);
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
		return deserializeObject(is, DefaultRequest.class);
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
		return deserializeObject(is, DefaultResponse.class);
	}

	@Override
	public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
		serializeRequest(os, obj);
	}

}
