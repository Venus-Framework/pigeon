package com.dianping.pigeon.util;

import com.dianping.pigeon.log.LoggerLoader;
import com.facebook.swift.codec.metadata.ReflectionHelper;
import com.facebook.swift.service.ThriftService;
import com.dianping.pigeon.log.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author qi.yin
 *         2016/05/16  下午3:12.
 */
public class ThriftUtils {

    private static final Logger logger = LoggerLoader.getLogger(ThriftUtils.class);

    public static boolean isSupportedThrift(Class<?> clazz) {
        return (isAnnotation(clazz) || isIDL(clazz));
    }

    public static boolean isAnnotation(Class<?> clazz) {
        Set<ThriftService> serviceAnnotations = ReflectionHelper
                .getEffectiveClassAnnotations(
                        clazz, ThriftService.class);

        if (serviceAnnotations.size() == 1) {
            return true;
        } else if (serviceAnnotations.size() > 1) {

            logger.error("Service class" + clazz.getName() +
                    "has multiple conflicting @ThriftService annotations:"
                    + serviceAnnotations);

        }
        return false;
    }

    public static boolean isIDL(Class<?> clazz) {
        String name = clazz.getName();
        int index = name.indexOf("$");
        String clazzType;
        if (index < 0) {
            return false;
        }
        clazzType = name.substring(0, index);
        Class<?> claz = null;
        try {
            claz = ClassUtils.loadClass(clazzType);
        } catch (ClassNotFoundException e) {
            return false;
        }

        Class<?>[] classes = claz.getClasses();

        Set<String> classNames = new HashSet<String>();

        for (Class c : classes) {
            classNames.add(c.getSimpleName());
        }

        if (classNames.contains("Iface") && classNames.contains("AsyncIface")
                && classNames.contains("Client") && classNames.contains("AsyncClient")
                && classNames.contains("Processor")) {
            return true;
        }

        return false;
    }


    public static String generateSetMethodName(String fieldName) {

        return new StringBuilder(16)
                .append("set")
                .append(Character.toUpperCase(fieldName.charAt(0)))
                .append(fieldName.substring(1))
                .toString();

    }

    public static String generateGetMethodName(String fieldName) {
        return new StringBuffer(16)
                .append("get")
                .append(Character.toUpperCase(fieldName.charAt(0)))
                .append(fieldName.substring(1))
                .toString();
    }

    public static String generateBoolMethodName(String fieldName) {
        return new StringBuffer(16)
                .append("is")
                .append(Character.toUpperCase(fieldName.charAt(0)))
                .append(fieldName.substring(1))
                .toString();
    }

    public static String generateMethodArgsClassName(String serviceName, String methodName) {

        int index = serviceName.indexOf("$");

        if (index > 0) {
            return new StringBuilder(32)
                    .append(serviceName.substring(0, index + 1))
                    .append(methodName)
                    .append("_args")
                    .toString();
        }

        return null;

    }

    public static String generateMethodResultClassName(String serviceName, String methodName) {

        int index = serviceName.indexOf("$");

        if (index > 0) {
            return new StringBuilder(32)
                    .append(serviceName.substring(0, index + 1))
                    .append(methodName)
                    .append("_result")
                    .toString();
        }

        return null;

    }

}
