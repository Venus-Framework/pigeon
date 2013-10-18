package com.dianping.pigeon.component.xmap.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dianping.pigeon.component.xmap.annotation.XMemberAnnotation;

@XMemberAnnotation(XMemberAnnotation.NODE_MAP_SPRING)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface XNodeMapSpring {

	/**
	 * 
	 * @return the node xpath
	 */
	String value();

	/**
	 * 
	 * @return
	 */
	boolean trim() default true;

	/**
	 * 
	 * @return
	 */
	String key();

	/**
	 * 
	 * @return the type of items
	 */
	Class<?> type();

	/**
	 * 
	 * @return the type of items
	 */
	Class<?> componentType();
}
