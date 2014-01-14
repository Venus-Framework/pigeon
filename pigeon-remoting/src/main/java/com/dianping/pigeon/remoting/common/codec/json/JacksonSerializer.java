/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;

import org.apache.commons.lang.SerializationException;
import org.apache.log4j.Logger;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.codec.Serializer;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.process.InvocationHandlerFactory;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationProxy;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerializer implements Serializer {

	private static final Logger logger = LoggerLoader.getLogger(JacksonSerializer.class);

	ObjectMapper mapper = new ObjectMapper();

	public JacksonSerializer() {
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
		mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
		mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
		mapper.disable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
		mapper.disable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		// initialize
		String content = serializeObject(new DefaultRequest());
		deserializeObject(DefaultRequest.class, content);
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
			return mapper.readValue(sw.toByteArray(), clazz);
		} catch (Exception e) {
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
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}

	public <T> T deserializeObject(Class<T> objType, String content) throws SerializationException {
		try {
			return mapper.readValue(content, objType);
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
		try {
			mapper.writeValue(os, obj);
		} catch (JsonGenerationException e) {
			throw new SerializationException(e);
		} catch (JsonMappingException e) {
			throw new SerializationException(e);
		} catch (IOException e) {
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

	@Override
	public Object proxyRequest(InvokerConfig<?> invokerConfig) throws SerializationException {
		return Proxy.newProxyInstance(ProxyBeanFactory.class.getClassLoader(), new Class[] { invokerConfig
				.getServiceInterface() },
				new ServiceInvocationProxy(invokerConfig, InvocationHandlerFactory.createInvokeHandler(invokerConfig)));
	}

	@Override
	public InvocationResponse newResponse() throws SerializationException {
		return new DefaultResponse();
	}

	@Override
	public InvocationRequest newRequest(InvokerContext invokerContext) throws SerializationException {
		return new DefaultRequest(invokerContext);
	}

}
