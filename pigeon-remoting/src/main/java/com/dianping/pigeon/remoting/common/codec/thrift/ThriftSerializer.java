package com.dianping.pigeon.remoting.common.codec.thrift;

import com.dianping.pigeon.remoting.common.codec.AbstractSerializer;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.GenericRequest;
import com.dianping.pigeon.remoting.common.domain.generic.GenericResponse;
import com.dianping.pigeon.remoting.common.domain.generic.MessageType;
import com.dianping.pigeon.remoting.common.domain.generic.ThriftMapper;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.Header;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;
import com.dianping.pigeon.util.ThriftUtils;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qi.yin
 *         2016/06/27  上午11:21.
 */
public class ThriftSerializer extends AbstractSerializer {

    protected static final int HEADER_FIELD_LENGTH = 2;

    protected ServiceInvocationRepository repository = ServiceInvocationRepository.getInstance();

    private IDLThriftSerializer idlThriftSerializer = new IDLThriftSerializer();
    private AnnotationThriftSerializer annotationThriftSerializer = new AnnotationThriftSerializer();

    private ConcurrentHashMap<Class<?>, AbstractThriftSerializer> serializers =
            new ConcurrentHashMap<Class<?>, AbstractThriftSerializer>();

    @Override
    public Object deserializeRequest(InputStream is) throws SerializationException {
        GenericRequest request = null;

        TIOStreamTransport transport = new TIOStreamTransport(is);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);

        try {
            //headerLength
            protocol.readI16();

            //header
            Header header = new Header();
            header.read(protocol);

            if (header.getRequestInfo() == null) {
                throw new SerializationException("Deserialize requestInfo is no legal. header " + header);
            }

            request = ThriftMapper.convertHeaderToRequest(header);

            if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
                //body
                Class<?> iface = ServicePublisher.getInterface(request.getServiceName());

                if (iface == null) {
                    throw new SerializationException("Deserialize thrift serviceName is invalid.");
                }

                request.setServiceInterface(iface);

                AbstractThriftSerializer serializer = getSerializer(iface);

                serializer.doDeserializeRequest(request, protocol);
            }

        } catch (Exception e) {
            throw new SerializationException("Deserialize request failed.", e);
        }
        return request;
    }

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
                protocol.writeI16(Short.MAX_VALUE);

                //header
                Header header = ThriftMapper.convertRequestToHeader(request);
                header.write(protocol);

                short headerLength = (short) (bos.size() - HEADER_FIELD_LENGTH);

                if (header.getMessageType() == MessageType.Normal.getCode()) {

                    Class<?> iface = request.getServiceInterface();

                    if (iface == null) {
                        throw new SerializationException("Serialize thrift interface is null.");
                    }

                    AbstractThriftSerializer serializer = getSerializer(iface);

                    serializer.doSerializeRequest(request, protocol);
                }
                int messageLength = bos.size();

                try {
                    bos.setWriteIndex(0);
                    protocol.writeI16(headerLength);
                } finally {
                    bos.setWriteIndex(messageLength);
                }

                os.write(bos.toByteArray());
            } catch (Exception e) {
                throw new SerializationException("serialize request failed.", e);
            }
        }
    }

    @Override
    public Object deserializeResponse(InputStream is) throws SerializationException {
        GenericResponse response = null;

        TIOStreamTransport transport = new TIOStreamTransport(is);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);

        try {
            //headerLength
            protocol.readI16();
            //header
            Header header = new Header();
            header.read(protocol);

            if (header.getResponseInfo() == null) {
                throw new SerializationException("Deserialize response is no legal. header " + header);
            }

            response = ThriftMapper.convertHeaderToResponse(header);

            if (header.getMessageType() == MessageType.Normal.getCode()) {

                GenericRequest request = (GenericRequest) repository.get(
                        header.getResponseInfo().getSequenceId());

                if (request == null) {
                    throw new SerializationException("Deserialize cannot find related request. sequenceId " + header.getResponseInfo().getSequenceId());
                }

                Class<?> iface = request.getServiceInterface();

                if (iface == null) {
                    throw new SerializationException("Deserialize interface is null.");
                }

                AbstractThriftSerializer serializer = getSerializer(iface);
                //body
                serializer.doDeserializeResponse(response, request, protocol, header);
            }

        } catch (Exception e) {
            throw new SerializationException("Deserialize response failed.", e);
        }
        return response;
    }

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
                protocol.writeI16(Short.MAX_VALUE);

                //header
                Header header = ThriftMapper.convertResponseToHeader(response);

                if (header.getMessageType() == MessageType.Normal.getCode()) {
                    Class<?> iface = ServicePublisher.getInterface(response.getServiceName());

                    if (iface == null) {
                        throw new SerializationException("Serialize thrift serviceName is invalid.");
                    }

                    response.setServiceInterface(iface);

                    AbstractThriftSerializer serializer = getSerializer(iface);
                    //body
                    serializer.doSerializeResponse(response, protocol, header, bos);

                } else {
                    //header
                    header.write(protocol);
                    short headerLength = (short) (bos.size() - HEADER_FIELD_LENGTH);
                    int messageLength = bos.size();

                    try {
                        bos.setWriteIndex(0);
                        protocol.writeI16(headerLength);
                    } finally {
                        bos.setWriteIndex(messageLength);
                    }
                }

                os.write(bos.toByteArray());
            } catch (Exception e) {
                throw new SerializationException("Serialize failed.", e);
            }
        }
    }

    @Override
    public InvocationResponse newResponse() throws SerializationException {
        return new GenericResponse();
    }

    @Override
    public InvocationRequest newRequest(InvokerContext invokerContext) throws SerializationException {
        return new GenericRequest(invokerContext);
    }

    protected AbstractThriftSerializer getSerializer(Class<?> clazz) {
        AbstractThriftSerializer serializer = serializers.get(clazz);

        if (serializer == null) {
            if (ThriftUtils.isAnnotation(clazz)) {
                serializer = annotationThriftSerializer;
                serializers.putIfAbsent(clazz, annotationThriftSerializer);
            } else if (ThriftUtils.isIDL(clazz)) {
                serializer = idlThriftSerializer;
                serializers.putIfAbsent(clazz, idlThriftSerializer);
            } else {
                throw new SerializationException("Service interface " + clazz.getName() +
                        " do not support thrift serialize");
            }
        }

        return serializer;
    }

}
