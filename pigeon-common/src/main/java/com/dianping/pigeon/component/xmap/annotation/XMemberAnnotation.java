package com.dianping.pigeon.component.xmap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to identify XMap annotations.
 * <p>
 * This annotation has a single parameter "value" of type <code>int</code> that
 * specifies the type of the annotation.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface XMemberAnnotation {

	int NODE = 1;
	int NODE_LIST = 2;
	int NODE_MAP = 3;
	int PARENT = 4;
	int CONTENT = 5;
	int NODE_SPRING = 6;
	int NODE_LIST_SPRING = 7;
	int NODE_MAP_SPRING = 8;

	/**
	 * The type of the annotation.
	 * 
	 * @return
	 */
	int value();

}
