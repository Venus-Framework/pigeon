package com.dianping.pigeon.remoting.common.codec.thrift;

import com.dianping.pigeon.remoting.common.codec.AbstractSerializer;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.GenericRequest;
import com.dianping.pigeon.remoting.common.domain.generic.GenericResponse;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.Header;
import com.dianping.pigeon.remoting.common.domain.generic.ThriftMapper;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import com.dianping.pigeon.util.ClassUtils;
import com.dianping.pigeon.util.ThriftUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.transport.TIOStreamTransport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qi.yin
 *         2016/05/16  下午3:10.
 */
public class ThriftSerializer extends AbstractSerializer {

    private static final AtomicInteger SEQ_ID = new AtomicInteger(0);

    private static ConcurrentHashMap<String, Class<?>> cachedClass = new ConcurrentHashMap<String, Class<?>>();

    private static final int HEADER_FIELD_LENGTH = 4;
    private static final int BODY_FIELD_LENGTH = 4;

    private static final int FIELD_LENGTH = HEADER_FIELD_LENGTH + BODY_FIELD_LENGTH;

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
            if (message.type == TMessageType.CALL) {
                String argsClassName = ThriftClassNameGenerator.generateArgsClassName(
                        header.getRequestInfo().getServiceName(),
                        message.name);

                if (StringUtils.isEmpty(argsClassName)) {
                    throw new SerializationException("Deserialize thrift argsClassName is empty.");
                }

                Class clazz = cachedClass.get(argsClassName);

                if (clazz == null) {
                    try {
                        clazz = ClassUtils.loadClass(argsClassName);
                        cachedClass.putIfAbsent(argsClassName, clazz);
                    } catch (ClassNotFoundException e) {
                        throw new SerializationException("Deserialize class" + argsClassName + " load failed.", e);
                    }
                }

                TBase args;
                try {
                    args = (TBase) clazz.newInstance();
                } catch (InstantiationException e) {
                    throw new SerializationException("Deserialize class" + argsClassName + " new instance failed.", e);
                } catch (IllegalAccessException e) {
                    throw new SerializationException("Deserialize class" + argsClassName + " new instance failed.", e);
                }

                args.read(protocol);
                protocol.readMessageEnd();
                List<Object> parameters = new ArrayList<Object>();
                List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
                int index = 1;

                while (true) {

                    TFieldIdEnum fieldIdEnum = args.fieldForId(index++);

                    if (fieldIdEnum == null) {
                        break;
                    }

                    String fieldName = fieldIdEnum.getFieldName();

                    String getMethodName = ThriftUtils.generateGetMethodName(fieldName);

                    Method getMethod;

                    try {
                        getMethod = clazz.getMethod(getMethodName);
                    } catch (NoSuchMethodException e) {
                        throw new SerializationException(e);
                    }

                    parameterTypes.add(getMethod.getReturnType());
                    try {
                        parameters.add(getMethod.invoke(args));
                    } catch (IllegalAccessException e) {
                        throw new SerializationException(e);
                    } catch (InvocationTargetException e) {
                        throw new SerializationException(e);
                    }

                }

                request = ThriftMapper.convertHeaderToRequest(header);
                request.setMethodName(message.name);
                request.setParameters(parameters.toArray());
                request.setParameterTypes(parameterTypes.toArray(new Class[parameterTypes.size()]));
            }
            protocol.readMessageEnd();

        } catch (TException e) {
            throw new SerializationException("Unsupported this request obj serialize.");
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

                TMessage message = new TMessage(
                        request.getMethodName(),
                        TMessageType.CALL,
                        nextSeqId());

                String argsClassName = ThriftClassNameGenerator.generateArgsClassName(
                        request.getServiceName(),
                        request.getMethodName());

                if (StringUtils.isEmpty(argsClassName)) {
                    throw new SerializationException("Serialize thrift argsClassName is empty.");
                }

                Class clazz = cachedClass.get(argsClassName);

                if (clazz == null) {
                    try {
                        clazz = ClassUtils.loadClass(argsClassName);
                        cachedClass.putIfAbsent(argsClassName, clazz);
                    } catch (ClassNotFoundException e) {
                        throw new SerializationException("Serialize class" + argsClassName + " load failed.", e);
                    }
                }

                TBase args;
                try {
                    args = (TBase) clazz.newInstance();
                } catch (InstantiationException e) {
                    throw new SerializationException("Serialize class" + argsClassName + " new instance failed.", e);
                } catch (IllegalAccessException e) {
                    throw new SerializationException("Serialize class" + argsClassName + " new instance failed.", e);
                }

                for (int i = 0; i < request.getParameters().length; i++) {

                    Object paramObj = request.getParameters()[i];

                    if (paramObj == null) {
                        continue;
                    }

                    TFieldIdEnum field = args.fieldForId(i + 1);

                    String setMethodName = ThriftUtils.generateSetMethodName(field.getFieldName());

                    Method method;

                    try {
                        method = clazz.getMethod(setMethodName, request.getParameterTypes()[i]);
                    } catch (NoSuchMethodException e) {
                        throw new SerializationException("", e);
                    }

                    try {
                        method.invoke(args, obj);
                    } catch (IllegalAccessException e) {
                        throw new SerializationException("Serialize set args failed.", e);
                    } catch (InvocationTargetException e) {
                        throw new SerializationException("Serialize set args failed.", e);
                    }

                }
                //bodylength
                protocol.writeI32(Integer.MAX_VALUE);
                //body
                protocol.writeMessageBegin(message);
                args.write(protocol);
                protocol.writeMessageEnd();
                protocol.getTransport().flush();
                int messageLength = bos.size();
                int bodyLength = messageLength - headerLength - FIELD_LENGTH;

                // fill in message length and header length
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

            if (message.type == TMessageType.REPLY) {

                String resultClassName = ThriftClassNameGenerator.generateResultClassName(
                        serviceName,
                        message.name);

                if (StringUtils.isEmpty(resultClassName)) {
                    throw new SerializationException("Deserialize thrift resultClassName is empty.");
                }
                Class<?> clazz = cachedClass.get(resultClassName);

                if (clazz == null) {

                    try {

                        clazz = ClassUtils.loadClass(resultClassName);

                        cachedClass.putIfAbsent(resultClassName, clazz);

                    } catch (ClassNotFoundException e) {
                        throw new SerializationException("Deserialize failed.", e);
                    }

                }

                TBase<?, ? extends TFieldIdEnum> result;
                try {
                    result = (TBase<?, ?>) clazz.newInstance();
                } catch (InstantiationException e) {
                    throw new SerializationException("Deserialize failed.", e);
                } catch (IllegalAccessException e) {
                    throw new SerializationException("Deserialize failed.", e);
                }

                try {
                    result.read(protocol);
                } catch (TException e) {
                    throw new SerializationException("Deserialize failed.", e);
                }

                Object realResult = null;

                int index = 0;

                while (true) {

                    TFieldIdEnum fieldIdEnum = result.fieldForId(index++);

                    if (fieldIdEnum == null) {
                        break;
                    }

                    Field field;

                    try {
                        field = clazz.getDeclaredField(fieldIdEnum.getFieldName());
                        field.setAccessible(true);
                    } catch (NoSuchFieldException e) {
                        throw new SerializationException("Deserialize failed.", e);
                    }

                    try {
                        realResult = field.get(result);
                    } catch (IllegalAccessException e) {
                        throw new SerializationException("Deserialize failed.", e);
                    }

                    if (realResult != null) {
                        break;
                    }

                }
            } else if (message.type == TMessageType.EXCEPTION) {
                TApplicationException exception = TApplicationException.read(protocol);
                ThriftMapper.mapException(header, response, exception.getMessage());
            }

            protocol.readMessageEnd();

        } catch (Exception e) {
            throw new SerializationException("Deserialize request failed.", e);
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

                TIOStreamTransport transport = new TIOStreamTransport(os);
                TBinaryProtocol protocol = new TBinaryProtocol(transport);

                //headerlength
                protocol.writeI32(Integer.MAX_VALUE);
                //header
                Header header = ThriftMapper.convertResponseToHeader(response, false);
                header.write(protocol);

                int headerLength = bos.size() - HEADER_FIELD_LENGTH;

                String resultClassName = ThriftClassNameGenerator.generateResultClassName(
                        response.getServiceName(),
                        response.getMethodName());

                if (StringUtils.isEmpty(resultClassName)) {
                    throw new SerializationException("Serialize thrift resultClassName is empty.");
                }

                Class clazz = cachedClass.get(resultClassName);

                if (clazz == null) {

                    try {
                        clazz = ClassUtils.loadClass(resultClassName);
                        cachedClass.putIfAbsent(resultClassName, clazz);
                    } catch (ClassNotFoundException e) {
                        throw new SerializationException("Serialize failed.", e);
                    }

                }

                TBase resultObj;

                try {
                    resultObj = (TBase) clazz.newInstance();
                } catch (InstantiationException e) {
                    throw new SerializationException("Serialize failed.", e);
                } catch (IllegalAccessException e) {
                    throw new SerializationException("Serialize failed.", e);
                }

                TApplicationException applicationException = null;
                TMessage message;

                if (response.hasException()) {
                    Throwable throwable = (Throwable) response.getReturn();
                    int index = 1;
                    boolean found = false;
                    while (true) {
                        TFieldIdEnum fieldIdEnum = resultObj.fieldForId(index++);
                        if (fieldIdEnum == null) {
                            break;
                        }
                        String fieldName = fieldIdEnum.getFieldName();
                        String getMethodName = ThriftUtils.generateGetMethodName(fieldName);
                        String setMethodName = ThriftUtils.generateSetMethodName(fieldName);
                        Method getMethod;
                        Method setMethod;
                        try {
                            getMethod = clazz.getMethod(getMethodName);
                            if (getMethod.getReturnType().equals(throwable.getClass())) {
                                found = true;
                                setMethod = clazz.getMethod(setMethodName, throwable.getClass());
                                setMethod.invoke(resultObj, throwable);
                            }
                        } catch (NoSuchMethodException e) {
                            throw new SerializationException("Serialize failed.", e);
                        } catch (InvocationTargetException e) {
                            throw new SerializationException("Serialize failed.", e);
                        } catch (IllegalAccessException e) {
                            throw new SerializationException("Serialize failed.", e);
                        }
                    }

                    if (!found) {
                        applicationException = new TApplicationException(throwable.getMessage());
                    }

                } else {
                    Object realResult = response.getReturn();
                    // result field id is 0
                    String fieldName = resultObj.fieldForId(0).getFieldName();
                    String setMethodName = ThriftUtils.generateSetMethodName(fieldName);
                    String getMethodName = ThriftUtils.generateGetMethodName(fieldName);
                    Method getMethod;
                    Method setMethod;
                    try {
                        getMethod = clazz.getMethod(getMethodName);
                        setMethod = clazz.getMethod(setMethodName, getMethod.getReturnType());
                        setMethod.invoke(resultObj, realResult);
                    } catch (NoSuchMethodException e) {
                        throw new SerializationException("Serialize failed.", e);
                    } catch (InvocationTargetException e) {
                        throw new SerializationException("Serialize failed.", e);
                    } catch (IllegalAccessException e) {
                        throw new SerializationException("Serialize failed.", e);
                    }

                }

                if (applicationException != null) {
                    message = new TMessage(response.getMethodName(), TMessageType.EXCEPTION, nextSeqId());
                } else {
                    message = new TMessage(response.getMethodName(), TMessageType.REPLY, nextSeqId());
                }

                protocol.writeMessageBegin(message);
                switch (message.type) {
                    case TMessageType.EXCEPTION:
                        applicationException.write(protocol);
                        break;
                    case TMessageType.REPLY:
                        resultObj.write(protocol);
                        break;
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

    private static int nextSeqId() {
        return SEQ_ID.incrementAndGet();
    }
}
