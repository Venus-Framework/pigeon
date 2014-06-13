/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PigeonAutoTest {

    String env() default "DEV";

    String vip() default "";

    String registry() default "zookeeper";
    
    String registryServer() default "127.0.0.1:2181";
    
    String url() default "";
    
    String protocol() default "";

    String serialize() default "hessian";

    String callMethod() default "sync";

    int timeout() default 5000;

    String callback() default "null";

    String loadbalance() default "";

    String cluster() default "failFast";

    int retries() default 1;

    boolean timeoutRetry() default false;

    String group() default "";

    String zone() default "";

}
