package com.dianping.pigeon.remoting.common.codec.fst;

import java.io.InputStream;
import java.io.OutputStream;

import com.dianping.avatar.tracker.CacheExecutionTrace;
import com.dianping.avatar.tracker.SqlExecutionTrace;
import com.dianping.avatar.tracker.TrackerContext;
import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.remoting.common.codec.DefaultAbstractSerializer;
import com.dianping.pigeon.remoting.common.exception.SerializationException;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

public class FstSerializer extends DefaultAbstractSerializer {

	static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

	public FstSerializer() {
		conf.registerClass(DefaultRequest.class, DefaultResponse.class, TrackerContext.class,
				CacheExecutionTrace.class, SqlExecutionTrace.class);
	}

	public Object deserializeObject(InputStream is) throws SerializationException {
		try {
			FSTObjectInput in = conf.getObjectInput(is);
			Object result = in.readObject();
			is.close();
			return result;
		} catch (Throwable e) {
			throw new SerializationException(e.getMessage(), e);
		}
	}

	@Override
	public Object deserializeRequest(InputStream is) throws SerializationException {
		return deserializeObject(is);
	}

	@Override
	public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
		try {
			FSTObjectOutput out = conf.getObjectOutput(os);
			out.writeObject(obj);
			out.flush();
			os.close();
		} catch (Throwable e) {
			throw new SerializationException(e.getMessage(), e);
		}
	}

	@Override
	public Object deserializeResponse(InputStream is) throws SerializationException {
		return deserializeObject(is);
	}

	@Override
	public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
		serializeRequest(os, obj);
	}

}
