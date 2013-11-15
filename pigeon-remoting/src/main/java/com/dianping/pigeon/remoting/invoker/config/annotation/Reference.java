/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Reference {

	Class<?> interfaceClass() default void.class;
	
	String interfaceName();
	
    String vip();
    
    String url();

    String serialize() default Constants.SERIALIZE_HESSIAN;

    String callMethod() default Constants.CALL_SYNC;

    int timeout() default 2000;

    String callback();

    String loadbalance() default LoadBalanceManager.DEFAULT_LOADBALANCE;

    String cluster() default "failFast";

    int retries() default 1;

    boolean timeoutRetry() default false;
    
    String version();
    
    String group();

}
