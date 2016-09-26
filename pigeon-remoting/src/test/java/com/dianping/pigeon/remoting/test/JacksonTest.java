package com.dianping.pigeon.remoting.test;

import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/9/2.
 */
public class JacksonTest {

    @Test
    public void test1() {
        parseMethodAppLimitConfig("{\"api#method\" : {\"invoker1\":50, \"invoker2\":100}}");

        System.out.println("ok");
    }

    // api#method --> {app1 --> qpslimit, app2 --> qpslimit}
    private static volatile Map<String, Map<String, Long>> methodAppLimitMap = Maps.newConcurrentMap();
    private static final JacksonSerializer jacksonSerializer = new JacksonSerializer();
    private static void parseMethodAppLimitConfig(String methodAppLimitConfig) {
        if (StringUtils.isNotBlank(methodAppLimitConfig)) {
            Map<String, Map<String, Long>> map = Maps.newConcurrentMap();
            try {
                map = (HashMap) jacksonSerializer.toObject(HashMap.class, methodAppLimitConfig);
                methodAppLimitMap.clear();
                methodAppLimitMap = new ConcurrentHashMap<>(map);
            } catch (Throwable t) {
                System.out.println(t.toString());
            }
        }
    }
}
