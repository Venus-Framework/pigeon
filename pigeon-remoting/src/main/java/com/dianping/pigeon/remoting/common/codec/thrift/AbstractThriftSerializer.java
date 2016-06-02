package com.dianping.pigeon.remoting.common.codec.thrift;

import com.dianping.pigeon.remoting.common.codec.AbstractSerializer;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.GenericRequest;
import com.dianping.pigeon.remoting.common.domain.generic.GenericResponse;
import com.dianping.pigeon.remoting.common.domain.generic.ThriftMapper;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.Header;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.transport.TIOStreamTransport;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qi.yin
 *         2016/06/02  下午3:39.
 */
public abstract class AbstractThriftSerializer extends AbstractSerializer {

    private static final int HEADER_FIELD_LENGTH = 4;

    private static final int BODY_FIELD_LENGTH = 4;

    private static final int FIELD_LENGTH = HEADER_FIELD_LENGTH + BODY_FIELD_LENGTH;

    private static final AtomicInteger SEQ_ID = new AtomicInteger(0);

    private ServiceInvocationRepository repository = ServiceInvocationRepository.getInstance();

    @Override
    public Object deserializeRequest(InputStream is) throws SerializationException {
        GenericRequest request = null;

        TIOStreamTransport transport = new TIOStreamTransport(is);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);

        try {
            //headerLength
            protocol.readI32();

            //header
            Header header = new Header();
            header.read(protocol);

            //bodyLength
            protocol.readI32();

            // body
            TMessage message = protocol.readMessageBegin();

            if (header.getRequestInfo() == null ||
                    StringUtils.isEmpty(header.getRequestInfo().getServiceName())) {
                throw new SerializationException("Deserialize requestInfo is no legal. header " + header);
            }

            String serviceName = header.getRequestInfo().getServiceName();

            request = ThriftMapper.convertHeaderToRequest(header);

        } catch (Exception e) {
            throw new SerializationException("Deserialize request failed.", e);
        }
        return request;
    }

    @Override
    public void serializeRequest(OutputStream os, Object obj) throws SerializationException {

    }

    @Override
    public Object deserializeResponse(InputStream is) throws SerializationException {
        return null;
    }

    @Override
    public void serializeResponse(OutputStream os, Object obj) throws SerializationException {

    }

    @Override
    public InvocationResponse newResponse() throws SerializationException {
        return new GenericResponse();
    }

    @Override
    public InvocationRequest newRequest(InvokerContext invokerContext) throws SerializationException {
        return new GenericRequest(invokerContext);
    }

    protected int getSequenceId() {
        return 1;
    }
}
