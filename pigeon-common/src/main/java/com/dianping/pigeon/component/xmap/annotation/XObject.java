package com.dianping.pigeon.component.xmap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface XObject {

	/**
	 * An xpath expression specifying the XML node to bind to.
	 * 
	 * @return the node xpath
	 */
	String value() default "";

	String[] order() default {};

}
