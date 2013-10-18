package com.dianping.pigeon.component.xmap.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dianping.pigeon.component.xmap.annotation.XMemberAnnotation;

@XMemberAnnotation(XMemberAnnotation.NODE_SPRING)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface XNodeSpring {

	String value();

	boolean trim() default true;

	Class<?> type() default Object.class;

}
