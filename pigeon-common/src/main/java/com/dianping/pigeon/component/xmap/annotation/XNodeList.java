package com.dianping.pigeon.component.xmap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@XMemberAnnotation(XMemberAnnotation.NODE_LIST)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface XNodeList {

	/**
	 * A path expression specifying the XML node to bind to.
	 * 
	 * @return the node xpath
	 */
	String value();

	/**
	 * Whether to trim text content for element nodes.
	 * 
	 * @return
	 */
	boolean trim() default true;

	/**
	 * The type of a collection object.
	 * 
	 * @return the type of items
	 */
	Class<?> type();

	/**
	 * The type of the objects in this collection.
	 * 
	 * @return the type of items
	 */
	Class<?> componentType();

	/**
	 * is or not cdata
	 */
	boolean cdata() default false;

}
