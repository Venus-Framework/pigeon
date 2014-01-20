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

import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Service {

	Class<?> interfaceClass() default void.class;

	String url() default "";

	String version() default "";

	String group() default "";

	int port() default ServerConfig.DEFAULT_PORT;

	int httpPort() default ServerConfig.DEFAULT_HTTP_PORT;

	boolean autoSelectPort() default true;

	boolean autoRegister() default true;

	int corePoolSize() default Constants.DEFAULT_PROVIDER_COREPOOLSIZE;

	int maxPoolSize() default Constants.DEFAULT_PROVIDER_MAXPOOLSIZE;

	int workQueueSize() default Constants.DEFAULT_PROVIDER_WORKQUEUESIZE;

}
