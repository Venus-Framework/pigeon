package com.dianping.pigeon.remoting.common.codec.thrift.annotation;

import com.dianping.pigeon.util.ObjectUtils;
import com.facebook.swift.codec.metadata.ThriftCatalog;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static com.facebook.swift.codec.metadata.ReflectionHelper.findAnnotatedMethods;
import static com.facebook.swift.codec.metadata.ReflectionHelper.getEffectiveClassAnnotations;

/**
 * @author qi.yin
 *         2016/05/23  下午6:07.
 */
@Immutable
public class ThriftServiceMetadata
{
    private final String name;
    private final Map<String, ThriftMethodMetadata> methods;
    private final Map<String, ThriftMethodMetadata> declaredMethods;
    private final ImmutableList<ThriftServiceMetadata> parentServices;
    private final ImmutableList<String> documentation;

    public ThriftServiceMetadata(Class<?> serviceClass, ThriftCatalog catalog)
    {
        Preconditions.checkNotNull(serviceClass, "serviceClass is null");
        ThriftService thriftService = getThriftServiceAnnotation(serviceClass);

        if (thriftService.value().length() == 0) {
            name = serviceClass.getSimpleName();
        }
        else {
            name = thriftService.value();
        }

        documentation = ThriftCatalog.getThriftDocumentation(serviceClass);

        ImmutableMap.Builder<String, ThriftMethodMetadata> builder = ImmutableMap.builder();

        Function<ThriftMethodMetadata, String> methodMetadataNamer = new Function<ThriftMethodMetadata, String>() {
            @Nullable
            @Override
            public String apply(@Nullable ThriftMethodMetadata methodMetadata)
            {
                return methodMetadata.getName();
            }
        };
        // A multimap from order to method name. Sorted by key (order), with nulls (i.e. no order) last.
        // Within each key, values (ThriftMethodMetadata) are sorted by method name.
        TreeMultimap<Integer, ThriftMethodMetadata> declaredMethods = TreeMultimap.create(
                Ordering.natural().nullsLast(),
                Ordering.natural().onResultOf(methodMetadataNamer));
        for (Method method : findAnnotatedMethods(serviceClass, ThriftMethod.class)) {
            if (method.isAnnotationPresent(ThriftMethod.class)) {
                ThriftMethodMetadata methodMetadata = new ThriftMethodMetadata(name, method, catalog);
                builder.put(methodMetadata.getName(), methodMetadata);
                if (method.getDeclaringClass().equals(serviceClass)) {
                    declaredMethods.put(ThriftCatalog.getMethodOrder(method), methodMetadata);
                }
            }
        }
        methods = builder.build();
        // create a name->metadata map keeping the order
        this.declaredMethods = Maps.uniqueIndex(declaredMethods.values(), methodMetadataNamer);

        ThriftServiceMetadata parentService = null;
        ImmutableList.Builder<ThriftServiceMetadata> parentServiceBuilder = ImmutableList.builder();
        for (Class<?> parent : serviceClass.getInterfaces()) {
            if (!getEffectiveClassAnnotations(parent, ThriftService.class).isEmpty()) {
                parentServiceBuilder.add(new ThriftServiceMetadata(parent, catalog));
            }
        }
        this.parentServices = parentServiceBuilder.build();
    }

    public ThriftServiceMetadata(String name, ThriftMethodMetadata... methods)
    {
        this.name = name;

        ImmutableMap.Builder<String, ThriftMethodMetadata> builder = ImmutableMap.builder();
        for (ThriftMethodMetadata method : methods) {
            builder.put(method.getName(), method);
        }
        this.methods = builder.build();
        this.declaredMethods = this.methods;
        this.parentServices = ImmutableList.of();
        this.documentation = ImmutableList.of();
    }

    public String getName()
    {
        return name;
    }

    public ThriftMethodMetadata getMethod(String name)
    {
        return methods.get(name);
    }

    public Map<String, ThriftMethodMetadata> getMethods()
    {
        return methods;
    }

    public Map<String, ThriftMethodMetadata> getDeclaredMethods()
    {
        return declaredMethods;
    }

    public ImmutableList<String> getDocumentation()
    {
        return documentation;
    }

    public static ThriftService getThriftServiceAnnotation(Class<?> serviceClass)
    {
        Set<ThriftService> serviceAnnotations = getEffectiveClassAnnotations(serviceClass, ThriftService.class);
        Preconditions.checkArgument(!serviceAnnotations.isEmpty(), "Service class %s is not annotated with @ThriftService", serviceClass.getName());
        Preconditions.checkArgument(serviceAnnotations.size() == 1,
                "Service class %s has multiple conflicting @ThriftService annotations: %s",
                serviceClass.getName(),
                serviceAnnotations
        );

        return Iterables.getOnlyElement(serviceAnnotations);
    }

    public ImmutableList<ThriftServiceMetadata> getParentServices()
    {
        return parentServices;
    }

    public ThriftServiceMetadata getParentService()
    {
        // Assert that we have 0 or 1 parent.
        // Having multiple @ThriftService parents is generally supported by swift,
        // but this is a restriction that applies to swift2thrift generator (because the Thrift IDL doesn't)
        Preconditions.checkState(parentServices.size() <= 1);

        if (parentServices.isEmpty()) {
            return null;
        } else {
            return parentServices.get(0);
        }
    }

    @Override
    public int hashCode()
    {
        return ObjectUtils.hash(name, methods, parentServices);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ThriftServiceMetadata other = (ThriftServiceMetadata) obj;
        return ObjectUtils.equals(this.name, other.name) && ObjectUtils.equals(this.methods, other.methods)
                && ObjectUtils.equals(this.parentServices, other.parentServices);
    }
}
