package com.dianping.pigeon.remoting.common.codec.thrift;

import com.dianping.pigeon.remoting.common.domain.generic.GenericRequest;
import com.dianping.pigeon.remoting.common.domain.generic.GenericResponse;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.Header;
import com.dianping.pigeon.remoting.common.domain.generic.ThriftMapper;
import com.dianping.pigeon.remoting.common.domain.generic.StatusCode;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.util.ClassUtils;
import com.dianping.pigeon.util.ThriftUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qi.yin
 *         2016/05/16  下午3:10.
 */
public class IDLThriftSerializer extends AbstractThriftSerializer {

    private static ConcurrentMap<String, Class<?>> cachedClass = new ConcurrentHashMap<String, Class<?>>();

    private static final String BYTE_ARRAY_CLASS_NAME = "[B";

    @Override
    protected void doDeserializeRequest(GenericRequest request, TProtocol protocol) throws Exception {
        TMessage message = protocol.readMessageBegin();

        if (message.type == TMessageType.CALL) {

            String argsClassName = ThriftClassNameGenerator.generateArgsClassName(
                    request.getServiceInterface().getName(),
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

                    try {
                        getMethod = clazz.getMethod(ThriftUtils.generateBoolMethodName(fieldName));
                    } catch (NoSuchMethodException e0) {
                        throw new SerializationException("Deserialize failed.", e);
                    }
                }

                Object getResult;
                try {
                    getResult = getMethod.invoke(args);
                } catch (IllegalAccessException e) {
                    throw new SerializationException("Deserialize failed.", e);
                } catch (InvocationTargetException e) {
                    throw new SerializationException("Deserialize failed.", e);
                }

                if (BYTE_ARRAY_CLASS_NAME.equals(getMethod.getReturnType().getName())) {
                    parameterTypes.add(ByteBuffer.class);
                    parameters.add(ByteBuffer.wrap((byte[]) getResult));
                } else {
                    parameterTypes.add(getMethod.getReturnType());
                    parameters.add(getResult);
                }

            }
            request.setSeqId(message.seqid);
            request.setMethodName(message.name);
            request.setParameters(parameters.toArray());
            request.setParameterTypes(parameterTypes.toArray(new Class[parameterTypes.size()]));
            protocol.readMessageEnd();
        }
    }

    protected void doSerializeRequest(GenericRequest request, TProtocol protocol)
            throws Exception {
        TMessage message = new TMessage(
                request.getMethodName(),
                TMessageType.CALL,
                getSequenceId());

        String argsClassName = ThriftClassNameGenerator.generateArgsClassName(
                request.getServiceInterface().getName(),
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

        if (request.getParameters() != null) {

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
                    throw new SerializationException("Serialize class" + setMethodName + " new instance failed.", e);
                }

                try {
                    method.invoke(args, paramObj);
                } catch (IllegalAccessException e) {
                    throw new SerializationException("Serialize set args failed.", e);
                } catch (InvocationTargetException e) {
                    throw new SerializationException("Serialize set args failed.", e);
                }

            }
        }
        //body
        protocol.writeMessageBegin(message);
        args.write(protocol);
        protocol.writeMessageEnd();
        protocol.getTransport().flush();
    }

    protected void doDeserializeResponse(GenericResponse response, GenericRequest request, TProtocol protocol, Header header)
            throws Exception {
        // body
        TMessage message = protocol.readMessageBegin();
        response.setSeqId(message.seqid);

        if (message.type == TMessageType.REPLY) {

            String resultClassName = ThriftClassNameGenerator.generateResultClassName(
                    request.getServiceInterface().getName(),
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
                    if (index == 1) {
                        continue;
                    }
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

            response.setReturn(realResult);
        } else if (message.type == TMessageType.EXCEPTION) {
            TApplicationException exception = TApplicationException.read(protocol);
            ThriftMapper.mapException(header, response, exception.getMessage());
        }

        protocol.readMessageEnd();
    }

    protected void doSerializeResponse(GenericResponse response, TProtocol protocol,
                                       Header header, DynamicByteArrayOutputStream bos)
            throws Exception {

        String resultClassName = ThriftClassNameGenerator.generateResultClassName(
                response.getServiceInterface().getName(),
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
                    try {
                        getMethod = clazz.getMethod(getMethodName);
                    } catch (NoSuchMethodException e) {
                        try {
                            getMethod = clazz.getMethod(ThriftUtils.generateBoolMethodName(fieldName));
                        } catch (NoSuchMethodException e0) {
                            throw new SerializationException("Serialize failed.", e);
                        }
                    }
                    if (getMethod.getReturnType().equals(throwable.getClass())) {
                        header.responseInfo.setStatus(StatusCode.ApplicationException.getCode());
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

                try {
                    getMethod = clazz.getMethod(getMethodName);
                } catch (NoSuchMethodException e) {
                    try {
                        getMethod = clazz.getMethod(ThriftUtils.generateBoolMethodName(fieldName));
                    } catch (NoSuchMethodException e0) {
                        throw new SerializationException("Serialize failed.", e);
                    }
                }

                Class<?> returnType = getMethod.getReturnType();
                if (BYTE_ARRAY_CLASS_NAME.equals(getMethod.getReturnType().getName())) {
                    returnType = ByteBuffer.class;
                }

                setMethod = clazz.getMethod(setMethodName, returnType);
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
            message = new TMessage(response.getMethodName(), TMessageType.EXCEPTION, response.getSeqId());
        } else {
            message = new TMessage(response.getMethodName(), TMessageType.REPLY, response.getSeqId());
        }

        //header
        header.write(protocol);

        short headerLength = (short) (bos.size() - HEADER_FIELD_LENGTH);

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

        try {
            bos.setWriteIndex(0);
            protocol.writeI16(headerLength);
        } finally {
            bos.setWriteIndex(messageLength);
        }
    }

}