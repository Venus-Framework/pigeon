/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.codec.DefaultAbstractSerializer;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerializer extends DefaultAbstractSerializer {

	private static final Logger logger = LoggerLoader.getLogger(JacksonSerializer.class);
	private static boolean deserializeMap = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.codec.jackson.deserializemap", true);

	static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		// mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		// mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
		// mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
		// mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
		// mapper.disable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
		// mapper.disable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		// initialize
		JacksonSerializer serializer = new JacksonSerializer();
		String content = serializer.serializeObject(new DefaultRequest());
		serializer.deserializeObject(DefaultRequest.class, content);
	}

	public JacksonSerializer() {
	}

	public static void registerClass(Class<?>... classes) {
		mapper.registerSubtypes(classes);
	}

	@Override
	public Object deserializeRequest(InputStream is) throws SerializationException {
		return doDeserialize(is, DefaultRequest.class);
	}

	public Object doDeserialize(InputStream is, Class<?> clazz) throws SerializationException {
		ByteArrayOutputStream sw = new ByteArrayOutputStream();
		byte[] buf = new byte[512];
		int len = -1;
		try {
			while ((len = is.read(buf)) != -1) {
				sw.write(buf, 0, len);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("deserialize:" + new String(sw.toByteArray()));
			}
			if (deserializeMap) {
				return JacksonObjectMapper.convertObject(mapper.readValue(sw.toByteArray(), clazz));
			} else {
				return mapper.readValue(sw.toByteArray(), clazz);
			}
		} catch (Throwable e) {
			throw new SerializationException(e);
		} finally {
			try {
				sw.close();
			} catch (IOException e) {
			}
		}
	}

	public String serializeObject(Object obj) throws SerializationException {
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (Throwable e) {
			throw new SerializationException(e);
		}
	}

	public <T> T deserializeObject(Class<T> objType, String content) throws SerializationException {
		try {
			if (deserializeMap) {
				return JacksonObjectMapper.convertObject(mapper.readValue(content, objType));
			} else {
				return mapper.readValue(content, objType);
			}
		} catch (Throwable e) {
			throw new SerializationException(e);
		}
	}

	public <T> T deserializeCollection(String content, Class<?> collectionClass, Class<?>... componentType)
			throws SerializationException {
		try {
			JavaType javaType = mapper.getTypeFactory().constructParametricType(collectionClass, componentType);
			return (T) mapper.readValue(content, javaType);
		} catch (Throwable e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
		try {
			mapper.writeValue(os, obj);
		} catch (Throwable e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public Object deserializeResponse(InputStream is) throws SerializationException {
		return doDeserialize(is, DefaultResponse.class);
	}

	@Override
	public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
		serializeRequest(os, obj);
	}

}
