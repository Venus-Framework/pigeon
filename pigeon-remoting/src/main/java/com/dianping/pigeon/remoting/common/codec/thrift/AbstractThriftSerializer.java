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
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qi.yin
 *         2016/06/02  下午3:39.
 */
public abstract class AbstractThriftSerializer extends AbstractSerializer {

    protected static final int HEADER_FIELD_LENGTH = 4;

    protected static final int BODY_FIELD_LENGTH = 4;

    protected static final int FIELD_LENGTH = HEADER_FIELD_LENGTH + BODY_FIELD_LENGTH;

    protected ServiceInvocationRepository repository = ServiceInvocationRepository.getInstance();

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

            if (header.getRequestInfo() == null ||
                    StringUtils.isEmpty(header.getRequestInfo().getServiceName())) {
                throw new SerializationException("Deserialize requestInfo is no legal. header " + header);
            }

            request = ThriftMapper.convertHeaderToRequest(header);
            //body
            doDeserializeRequest(request, protocol);

        } catch (Exception e) {
            throw new SerializationException("Deserialize request failed.", e);
        }
        return request;
    }

    protected abstract void doDeserializeRequest(GenericRequest request, TProtocol protocol) throws Exception;

    @Override
    public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
        if (!(obj instanceof GenericRequest)) {
            throw new SerializationException("Unsupported this request obj serialize.");
        } else {
            try {
                DynamicByteArrayOutputStream bos = new DynamicByteArrayOutputStream(1024);

                GenericRequest request = (GenericRequest) obj;
                TIOStreamTransport transport = new TIOStreamTransport(bos);
                TBinaryProtocol protocol = new TBinaryProtocol(transport);

                //headerlength
                protocol.writeI32(Integer.MAX_VALUE);

                //header
                Header header = ThriftMapper.convertRequestToHeader(request);
                header.write(protocol);

                int headerLength = bos.size() - HEADER_FIELD_LENGTH;

                doSerializeRequest(request, protocol);

                int messageLength = bos.size();
                int bodyLength = messageLength - headerLength - FIELD_LENGTH;

                try {
                    bos.setWriteIndex(0);
                    protocol.writeI32(headerLength);
                    bos.setWriteIndex(headerLength + HEADER_FIELD_LENGTH);
                    protocol.writeI32(bodyLength);
                } finally {
                    bos.setWriteIndex(messageLength);
                }

                os.write(bos.toByteArray());
            } catch (Exception e) {
                throw new SerializationException("serialize request failed.", e);
            }
        }
    }

    protected abstract void doSerializeRequest(GenericRequest request, TProtocol protocol)
            throws Exception;

    @Override
    public Object deserializeResponse(InputStream is) throws SerializationException {
        GenericResponse response = null;

        TIOStreamTransport transport = new TIOStreamTransport(is);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);

        try {
            //headerLength
            protocol.readI32();
            //header
            Header header = new Header();
            header.read(protocol);

            response = ThriftMapper.convertHeaderToResponse(header);

            //bodyLength
            protocol.readI32();

            if (header.getResponseInfo() == null) {
                throw new SerializationException("Deserialize response is no legal. header " + header);
            }
            //body
            doDeserializeResponse(response, protocol, header);

        } catch (Exception e) {
            throw new SerializationException("Deserialize response failed.", e);
        }
        return response;
    }

    protected abstract void doDeserializeResponse(GenericResponse response, TProtocol protocol, Header header)
            throws Exception;

    @Override
    public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
        if (!(obj instanceof GenericResponse)) {
            throw new SerializationException("Unsupported this response obj serialize.");
        } else {
            try {
                DynamicByteArrayOutputStream bos = new DynamicByteArrayOutputStream(1024);

                GenericResponse response = (GenericResponse) obj;

                TIOStreamTransport transport = new TIOStreamTransport(bos);
                TBinaryProtocol protocol = new TBinaryProtocol(transport);

                //headerlength
                protocol.writeI32(Integer.MAX_VALUE);

                //header
                Header header = ThriftMapper.convertResponseToHeader(response);

                doSerializeResponse(response, protocol, header, bos);

                os.write(bos.toByteArray());
            } catch (Exception e) {
                throw new SerializationException("Serialize failed.", e);
            }
        }
    }

    protected abstract void doSerializeResponse(GenericResponse response, TProtocol protocol,
                                                Header header, DynamicByteArrayOutputStream bos)
            throws Exception;

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
