package com.dianping.pigeon.remoting.common.codec.thrift.annotation;

import com.dianping.pigeon.util.ObjectUtils;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.facebook.swift.codec.metadata.*;
import com.facebook.swift.service.ThriftException;
import com.facebook.swift.service.ThriftMethod;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.thrift.TException;

import javax.annotation.concurrent.Immutable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.facebook.swift.codec.ThriftField.Requiredness;
import static com.facebook.swift.codec.metadata.FieldKind.THRIFT_FIELD;
import static com.facebook.swift.codec.metadata.ReflectionHelper.extractParameterNames;
import static com.google.common.base.Preconditions.checkState;

@Immutable
public class ThriftMethodMetadata
{
    private final String name;
    private final String qualifiedName;
    private final ThriftType returnType;
    private final List<ThriftFieldMetadata> parameters;
    private final Method method;
    private final ImmutableMap<Short, ThriftType> exceptions;
    private final ImmutableList<String> documentation;
    private final boolean oneway;

    public ThriftMethodMetadata(String serviceName, Method method, ThriftCatalog catalog)
    {
        Preconditions.checkNotNull(method, "method is null");
        Preconditions.checkNotNull(catalog, "catalog is null");

        this.method = method;

        ThriftMethod thriftMethod = method.getAnnotation(ThriftMethod.class);
        Preconditions.checkArgument(thriftMethod != null, "Method is not annotated with @ThriftMethod");

        Preconditions.checkArgument(!Modifier.isStatic(method.getModifiers()), "Method %s is static", method.toGenericString());

        if (thriftMethod.value().length() == 0) {
            name = method.getName();
        }
        else {
            name = thriftMethod.value();
        }
        this.qualifiedName = serviceName + "." + name;

        documentation = ThriftCatalog.getThriftDocumentation(method);
        returnType = catalog.getThriftType(method.getGenericReturnType());

        ImmutableList.Builder<ThriftFieldMetadata> builder = ImmutableList.builder();
        Type[] parameterTypes = method.getGenericParameterTypes();
        String[] parameterNames = extractParameterNames(method);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int index = 0; index < parameterTypes.length; index++) {
            ThriftField thriftField = null;
            for (Annotation annotation : parameterAnnotations[index]) {
                if (annotation instanceof ThriftField) {
                    thriftField = (ThriftField) annotation;
                    break;
                }
            }

            short parameterId = Short.MIN_VALUE;
            boolean isLegacyId = false;
            String parameterName = null;
            Requiredness parameterRequiredness = Requiredness.UNSPECIFIED;
            if (thriftField != null) {
                parameterId = thriftField.value();
                isLegacyId = thriftField.isLegacyId();
                parameterRequiredness = thriftField.requiredness();
                if (!thriftField.name().isEmpty()) {
                    parameterName = thriftField.name();
                }
            }
            if (parameterId == Short.MIN_VALUE) {
                parameterId = (short) (index + 1);
            }
            if (parameterName == null) {
                parameterName = parameterNames[index];
            }

            Type parameterType = parameterTypes[index];

            ThriftType thriftType = catalog.getThriftType(parameterType);

            ThriftInjection parameterInjection = new ThriftParameterInjection(parameterId, parameterName, index, parameterType);

            if (parameterRequiredness == Requiredness.UNSPECIFIED) {
                // There is only one field injection used to build metadata for method parameters, and if a
                // single injection point has UNSPECIFIED requiredness, that resolves to NONE.
                parameterRequiredness = Requiredness.NONE;
            }

            ThriftFieldMetadata fieldMetadata = new ThriftFieldMetadata(
                    parameterId,
                    isLegacyId,
                    parameterRequiredness,
                    thriftType,
                    parameterName,
                    THRIFT_FIELD,
                    ImmutableList.of(parameterInjection),
                    Optional.<ThriftConstructorInjection>absent(),
                    Optional.<ThriftMethodInjection>absent(),
                    Optional.<ThriftExtraction>absent(),
                    Optional.<TypeCoercion>absent()
            );
            builder.add(fieldMetadata);
        }
        parameters = builder.build();

        exceptions = buildExceptionMap(catalog, thriftMethod);

        this.oneway = thriftMethod.oneway();
    }

    public String getName()
    {
        return name;
    }

    public ThriftType getReturnType()
    {
        return returnType;
    }

    public List<ThriftFieldMetadata> getParameters()
    {
        return parameters;
    }

    public Map<Short, ThriftType> getExceptions()
    {
        return exceptions;
    }

    public ThriftType getException(short id)
    {
        return exceptions.get(id);
    }

    public ImmutableList<String> getDocumentation()
    {
        return documentation;
    }

    public Method getMethod()
    {
        return method;
    }

    public boolean getOneway() {
        return oneway;
    }

    private ImmutableMap<Short, ThriftType> buildExceptionMap(ThriftCatalog catalog,
                                                              ThriftMethod thriftMethod) {
        ImmutableMap.Builder<Short, ThriftType> exceptions = ImmutableMap.builder();
        Set<Type> exceptionTypes = new HashSet<Type>();
        int customExceptionCount = 0;

        if (thriftMethod.exception().length > 0) {
            for (ThriftException thriftException : thriftMethod.exception()) {
                exceptions.put(thriftException.id(), catalog.getThriftType(thriftException.type()));
                checkState(exceptionTypes.add(thriftException.type()), "ThriftMethod.exception contains more than one value for %s", thriftException.type());
            }
        }

        for (Class<?> exceptionClass : method.getExceptionTypes()) {
            if (exceptionClass.isAssignableFrom(TException.class)) {
                // the built-in exception types don't need special treatment
                continue;
            }

            if (exceptionClass.isAnnotationPresent(ThriftStruct.class)) {
                ++customExceptionCount;

                if (!exceptionTypes.contains(exceptionClass)) {
                    // there is no rhyme or reason to the order exception types are given to us,
                    // so we can only infer the id once
                    checkState(customExceptionCount <= 1, "ThriftMethod.exception annotation value must be specified when more than one custom exception is thrown.");
                    exceptions.put((short) 1, catalog.getThriftType(exceptionClass));
                }
            }
        }

        return exceptions.build();
    }

    public boolean isAsync()
    {
        Type returnType = method.getGenericReturnType();
        Class<?> rawType = TypeToken.of(returnType).getRawType();
        return ListenableFuture.class.isAssignableFrom(rawType);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ThriftMethodMetadata that = (ThriftMethodMetadata) o;

        return ObjectUtils.equals(name, that.name) && ObjectUtils.equals(returnType, that.returnType) &&
                ObjectUtils.equals(parameters, that.parameters) && ObjectUtils.equals(method, that.method) &&
                ObjectUtils.equals(exceptions, that.exceptions) && ObjectUtils.equals(oneway, that.oneway);
    }

    @Override
    public int hashCode()
    {
        return ObjectUtils.hash(name, returnType, parameters, method, exceptions, oneway);
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }
}
