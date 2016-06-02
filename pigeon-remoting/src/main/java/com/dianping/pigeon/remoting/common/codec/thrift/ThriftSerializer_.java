package com.dianping.pigeon.remoting.common.codec.thrift;

import com.dianping.pigeon.remoting.common.codec.AbstractSerializer;
import com.dianping.pigeon.remoting.common.codec.thrift.annotation.ThriftClientMetadata;
import com.dianping.pigeon.remoting.common.codec.thrift.annotation.ThriftMethodHandler;
import com.dianping.pigeon.remoting.common.codec.thrift.annotation.ThriftMethodProcessor;
import com.dianping.pigeon.remoting.common.codec.thrift.annotation.ThriftServerMetadata;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.GenericRequest;
import com.dianping.pigeon.remoting.common.domain.generic.GenericResponse;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.Header;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.StatusCode;
import com.dianping.pigeon.remoting.common.domain.generic.ThriftMapper;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import com.dianping.pigeon.util.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.transport.TIOStreamTransport;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qi.yin
 *         2016/05/23  下午4:28.
 */
public class ThriftSerializer_ extends AbstractSerializer {

    private ConcurrentHashMap<String, ThriftClientMetadata> clientMetadatas =
            new ConcurrentHashMap<String, ThriftClientMetadata>();

    private ConcurrentHashMap<String, ThriftServerMetadata> serverMetadatas =
            new ConcurrentHashMap<String, ThriftServerMetadata>();

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

            ThriftMethodProcessor methodProcessor = getMethodProcessor(
                    serviceName,
                    message.name);

            Object[] parameters = methodProcessor.readArguments(protocol);

            request = ThriftMapper.convertHeaderToRequest(header);
            request.setMethodName(message.name);
            request.setParameters(parameters);

            protocol.readMessageEnd();

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
                protocol.writeI32(Integer.MAX_VALUE);

                //header
                Header header = ThriftMapper.convertRequestToHeader(request);
                header.write(protocol);

                int headerLength = bos.size() - HEADER_FIELD_LENGTH;

                ThriftMethodHandler methodHandler = getMethodHandler(
                        request.getServiceName(),
                        request.getMethodName());

                //bodylength
                protocol.writeI32(Integer.MAX_VALUE);

                //body
                methodHandler.writeArguments(protocol,
                        nextSeqId(),
                        request.getParameters());

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

            // body
            TMessage message = protocol.readMessageBegin();

            if (header.getResponseInfo() == null) {
                throw new SerializationException("Deserialize response is no legal. header " + header);
            }

            InvocationRequest request = repository.get(
                    header.getResponseInfo().getSequenceId());

            if (request == null) {
                throw new SerializationException("Deserialize cannot find related request. header " + header);
            }

            String serviceName = request.getServiceName();
            ThriftMethodHandler methodHandler = getMethodHandler(
                    serviceName,
                    message.name);

            //body
            if (message.type == TMessageType.REPLY) {
                Object result = methodHandler.readResponse(protocol);
                response.setReturn(result);
            } else if (message.type == TMessageType.EXCEPTION) {
                TApplicationException exception = TApplicationException.read(protocol);
                ThriftMapper.mapException(header, response, exception.getMessage());
            }

            protocol.readMessageEnd();

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
                protocol.writeI32(Integer.MAX_VALUE);

                ThriftMethodProcessor methodProcessor = getMethodProcessor(
                        response.getServiceName(),
                        response.getMethodName());

                //header body
                Header header = ThriftMapper.convertResponseToHeader(
                        response,
                        methodProcessor.isUserException(response.getReturn()));

                header.write(protocol);

                int headerLength = bos.size() - HEADER_FIELD_LENGTH;

                //bodylength
                protocol.writeI32(Integer.MAX_VALUE);

                //body
                if (header.getResponseInfo().getStatus() == StatusCode.Success) {
                    protocol.writeMessageBegin(new TMessage(
                            response.getMethodName(),
                            TMessageType.REPLY,
                            nextSeqId()));

                    methodProcessor.writeResponse(protocol,
                            nextSeqId(),
                            response.getReturn());

                } else if (header.getResponseInfo().getStatus() == StatusCode.ApplicationException) {
                    protocol.writeMessageBegin(new TMessage(
                            response.getMethodName(),
                            TMessageType.REPLY,
                            nextSeqId()));

                    methodProcessor.writeExceptionResponse(protocol,
                            nextSeqId(),
                            response.getReturn());
                } else {
                    protocol.writeMessageBegin(new TMessage(
                            response.getMethodName(),
                            TMessageType.EXCEPTION,
                            nextSeqId()));

                    TApplicationException e = new TApplicationException(
                            ((Throwable) response.getReturn()).getMessage());
                    e.write(protocol);
                }

                protocol.writeMessageEnd();
                protocol.getTransport().flush();

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
                throw new SerializationException("Serialize response failed.", e);
            }
        }
    }

    @Override
    public InvocationResponse newResponse() throws SerializationException {
        return new GenericResponse();
    }

    @Override
    public InvocationRequest newRequest(InvokerContext invokerContext)
            throws SerializationException {
        return new GenericRequest(invokerContext);
    }

    private static int nextSeqId() {
        return SEQ_ID.incrementAndGet();
    }

    private ThriftMethodProcessor getMethodProcessor(String serviceName, String methodName)
            throws ClassNotFoundException {

        ThriftServerMetadata serverMetadata = serverMetadatas.get(serviceName);

        if (serverMetadata == null) {
            Class<?> serverType = ClassUtils.loadClass(serviceName);

            serverMetadata = new ThriftServerMetadata(serverType, serviceName);
            serverMetadatas.putIfAbsent(serviceName, serverMetadata);
        }

        return serverMetadata.getMethodProcessor(methodName);
    }

    private ThriftMethodHandler getMethodHandler(String serviceName, String methodName)
            throws ClassNotFoundException {

        ThriftClientMetadata clientMetadata = clientMetadatas.get(serviceName);

        if (clientMetadata == null) {
            Class<?> serverType = ClassUtils.loadClass(serviceName);

            clientMetadata = new ThriftClientMetadata(serverType, serviceName);
            clientMetadatas.putIfAbsent(serviceName, clientMetadata);
        }

        return clientMetadata.getMethodHandler(methodName);
    }
}
