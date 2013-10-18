package com.dianping.pigeon.component.xmap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that injects the content of the current node as an XML string
 * or DocumentFragment depending on the field type.
 * 
 */
@XMemberAnnotation(XMemberAnnotation.CONTENT)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface XContent {

	/**
	 * An xpathy expression specifying the XML node to bind to.
	 * 
	 * @return the node xpath
	 */
	String value() default "";

}
