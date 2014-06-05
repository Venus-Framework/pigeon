/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.route.balance.RoundRobinLoadBalance;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Reference {

	Class<?> interfaceClass() default void.class;

	String vip() default "";

	String url() default "";

	String protocol() default Constants.PROTOCOL_DEFAULT;

	String serialize() default Constants.SERIALIZE_HESSIAN;

	String callType() default Constants.CALL_SYNC;

	int timeout() default 2000;

	String callback() default "";

	String loadbalance() default RoundRobinLoadBalance.NAME;

	String cluster() default Constants.CLUSTER_FAILFAST;

	int retries() default 1;

	boolean timeoutRetry() default false;

	String version() default "";

	String group() default "";

}
