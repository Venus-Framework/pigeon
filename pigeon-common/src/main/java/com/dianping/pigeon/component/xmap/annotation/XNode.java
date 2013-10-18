package com.dianping.pigeon.component.xmap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@XMemberAnnotation(XMemberAnnotation.NODE)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface XNode {

	/**
	 * An xpathy expression specifying the XML node to bind to.
	 */
	String value() default "";

	/**
	 * Whether to trim text content for element nodes.
	 * <p>
	 * Ignored for attribute nodes.
	 * </p>
	 * 
	 */
	boolean trim() default true;

	/**
	 * is or not cdata
	 */
	boolean cdata() default false;
}
