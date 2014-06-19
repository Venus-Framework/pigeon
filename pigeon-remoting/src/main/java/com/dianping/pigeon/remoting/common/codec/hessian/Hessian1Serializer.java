/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.hessian;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.dianping.pigeon.remoting.common.codec.DefaultAbstractSerializer;
import com.dianping.pigeon.remoting.common.exception.SerializationException;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public class Hessian1Serializer extends DefaultAbstractSerializer {

	HessianSerializerFactory sessianSerializerFactory = new HessianSerializerFactory();

	@Override
	public Object deserializeResponse(InputStream is) throws SerializationException {
		return deserializeRequest(is);
	}

	@Override
	public Object deserializeRequest(InputStream is) throws SerializationException {
		HessianInput hin = new HessianInput(is);
		hin.setSerializerFactory(sessianSerializerFactory);
		try {
			return hin.readObject();
		} catch (Throwable t) {
			throw new SerializationException(t);
		} finally {
			try {
				hin.close();
			} catch (Exception e) {
				throw new SerializationException(e);
			}
		}
	}

	@Override
	public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
		serializeRequest(os, obj);
	}

	@Override
	public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
		HessianOutput hout = new HessianOutput(os);
		hout.setSerializerFactory(sessianSerializerFactory);
		try {
			hout.writeObject(obj);
			hout.flush();
		} catch (Throwable t) {
			throw new SerializationException(t);
		} finally {
			try {
				hout.close();
			} catch (IOException e) {
				throw new SerializationException(e);
			}
		}
	}

}
