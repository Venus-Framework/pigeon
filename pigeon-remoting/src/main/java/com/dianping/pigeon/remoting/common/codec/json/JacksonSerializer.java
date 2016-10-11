/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.codec.AbstractSerializer;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerializer extends AbstractSerializer {

	private static final Logger logger = LoggerLoader.getLogger(JacksonSerializer.class);
	static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

		// initialize
		JacksonSerializer serializer = new JacksonSerializer();
		String content = serializer.serializeObject(InvocationUtils.newRequest());
		serializer.deserializeObject(InvocationUtils.getRequestClass(), content);
	}

	public JacksonSerializer() {
	}

	public static void registerClass(Class<?>... classes) {
		mapper.registerSubtypes(classes);
	}

	@Override
	public Object deserializeRequest(InputStream is) throws SerializationException {
		return doDeserialize(is, InvocationUtils.getRequestClass());
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
			return this.toObject(clazz, new String(sw.toByteArray()));
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
			return JacksonObjectMapper.convertObject(mapper.readValue(content, objType));
		} catch (Throwable e) {
			throw new SerializationException(e);
		}
	}

	public <T> T readObject(Class<T> objType, String content) throws SerializationException {
		try {
			return mapper.readValue(content, objType);
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

	public <T> T deserializeArray(String content, Class<?> elementType) throws SerializationException {
		try {
			JavaType javaType = mapper.getTypeFactory().constructArrayType(elementType);
			return (T) mapper.readValue(content, javaType);
		} catch (Throwable e) {
			throw new SerializationException(e);
		}
	}

	public <T> T deserializeMap(String content, Class<?> mapClass, Class<?> keyClass, Class<?> valueClass)
			throws SerializationException {
		try {
			JavaType javaType = mapper.getTypeFactory().constructMapLikeType(mapClass, keyClass, valueClass);
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
		return doDeserialize(is, InvocationUtils.getResponseClass());
	}

	@Override
	public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
		serializeRequest(os, obj);
	}

	public Object toObject(Class<?> type, String value) throws SerializationException, ClassNotFoundException {
		if (value == null) {
			return null;
		}
		String value_;
		if (value.length() == 0) {
			value_ = "0";
		} else {
			value_ = value;
		}
		Object valueObj = value_;
		if (type == int.class || type == Integer.class) {
			valueObj = Integer.parseInt(value_);
		} else if (type == short.class || type == Short.class) {
			valueObj = Short.parseShort(value_);
		} else if (type == byte.class || type == Byte.class) {
			valueObj = Byte.parseByte(value_);
		} else if (type == char.class) {
			valueObj = value_;
		} else if (type == long.class || type == Long.class) {
			valueObj = Long.parseLong(value_);
		} else if (type == float.class || type == Float.class) {
			valueObj = Float.parseFloat(value_);
		} else if (type == double.class || type == Double.class) {
			valueObj = Double.parseDouble(value_);
		} else if (type == String.class) {
			valueObj = String.valueOf(value);
		} else {
			if (value == null || value.length() == 0) {
				valueObj = null;
			} else {
				valueObj = deserializeObject(type, value);
				if (valueObj instanceof Collection) {
					Collection valueObjList = (Collection) valueObj;
					if (!valueObjList.isEmpty()) {
						Object first = valueObjList.iterator().next();
						if (first instanceof Map) {
							Map valueMap = (Map) first;
							String valueClass = (String) valueMap.get("@class");
							if (valueClass != null) {
								valueObj = deserializeCollection(value, type, Class.forName(valueClass));
							}
						}
					}
				} else if (valueObj instanceof Map) {
					Map valueObjList = (Map) valueObj;
					if (!valueObjList.isEmpty()) {
						Map finalMap = new HashMap(valueObjList.size());
						valueObj = finalMap;
						String keyClass = null;
						String valueClass = null;
						try {
							for (Iterator ir = valueObjList.keySet().iterator(); ir.hasNext();) {
								Object k = ir.next();
								Object v = valueObjList.get(k);
								Object finalKey = k;
								Object finalValue = v;
								if (k instanceof String) {
									try {
										finalKey = deserializeObject(Map.class, (String) k);
									} catch (Throwable t) {
										if (keyClass == null) {
											Map firstValueMap = deserializeObject(Map.class, (String) k);
											if (firstValueMap != null) {
												keyClass = (String) firstValueMap.get("@class");
											}
										}
										if (keyClass != null) {
											finalKey = deserializeObject(Class.forName(keyClass), (String) k);
										}
									}
								}
								if (v instanceof String) {
									try {
										finalValue = deserializeObject(Map.class, (String) v);
									} catch (Throwable t) {
										if (valueClass == null) {
											Map firstValueMap = deserializeObject(Map.class, (String) v);
											if (firstValueMap != null) {
												valueClass = (String) firstValueMap.get("@class");
											}
										}
										if (valueClass != null) {
											finalValue = deserializeObject(Class.forName(valueClass), (String) v);
										}
									}
								}
								finalMap.put(finalKey, finalValue);
							}
						} catch (Throwable t) {
							valueObj = valueObjList;
						}
					}
				}
			}
		}
		return valueObj;
	}
}
