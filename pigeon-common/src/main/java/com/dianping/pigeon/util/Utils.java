package com.dianping.pigeon.util;

/**
 * Created by chenchongze on 16/5/16.
 */
public class Utils {

    public static String unescapeServiceName(String serviceName) {
        return serviceName.replace(Constants.PLACEHOLDER, Constants.PATH_SEPARATOR);
    }

    public static String escapeServiceName(String serviceName) {
        return serviceName.replace(Constants.PATH_SEPARATOR, Constants.PLACEHOLDER);
    }
}
