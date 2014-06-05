/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.hessian;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.dianping.pigeon.remoting.common.codec.DefaultAbstractSerializer;
import com.dianping.pigeon.remoting.common.exception.SerializationException;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public class HessianSerializer extends DefaultAbstractSerializer {

	@Override
	public Object deserializeResponse(InputStream is) throws SerializationException {
		return deserializeRequest(is);
	}

	@Override
	public Object deserializeRequest(InputStream is) throws SerializationException {
		Hessian2Input h2in = new Hessian2Input(is);
		try {
			return h2in.readObject();
		} catch (Throwable t) {
			throw new SerializationException(t);
		} finally {
			try {
				h2in.close();
			} catch (IOException e) {
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
		Hessian2Output h2out = new Hessian2Output(os);
		try {
			h2out.writeObject(obj);
			h2out.flush();
		} catch (Throwable t) {
			throw new SerializationException(t);
		} finally {
			try {
				h2out.close();
			} catch (IOException e) {
				throw new SerializationException(e);
			}
		}
	}

}
