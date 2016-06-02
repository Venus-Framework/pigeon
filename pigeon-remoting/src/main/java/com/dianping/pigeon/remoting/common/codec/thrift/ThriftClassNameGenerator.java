package com.dianping.pigeon.remoting.common.codec.thrift;

import com.dianping.pigeon.util.ThriftUtils;

/**
 * @author qi.yin
 *         2016/05/23  上午12:21.
 */
public class ThriftClassNameGenerator {

    public static String generateArgsClassName( String serviceName, String methodName ) {
        return ThriftUtils.generateMethodArgsClassName(serviceName, methodName);
    }

    public static String generateResultClassName( String serviceName, String methodName ) {
        return ThriftUtils.generateMethodResultClassName( serviceName, methodName );
    }

}
