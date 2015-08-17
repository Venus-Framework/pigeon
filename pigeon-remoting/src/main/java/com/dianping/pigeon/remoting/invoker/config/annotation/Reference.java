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

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Reference {

	Class<?> interfaceClass() default void.class;

	String vip() default "";

	String url() default "";

	String protocol() default "default";

	String serialize() default "hessian";

	String callType() default "sync";

	int timeout() default 5000;

	String callback() default "";

	String loadbalance() default "weightedAutoaware";

	String cluster() default "failfast";

	int retries() default 1;

	boolean timeoutRetry() default false;

	String version() default "";

	String group() default "";

}
