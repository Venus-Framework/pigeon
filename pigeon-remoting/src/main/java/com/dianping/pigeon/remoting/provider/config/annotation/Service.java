/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Service {

	Class<?> interfaceClass() default void.class;

	String url() default "";

	String version() default "";

	String group() default "";

	int port() default 4040;

	boolean autoSelectPort() default true;

	boolean useSharedPool() default true;
	
	int actives() default 0;
}
