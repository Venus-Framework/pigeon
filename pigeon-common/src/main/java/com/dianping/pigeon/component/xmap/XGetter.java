package com.dianping.pigeon.component.xmap;

public interface XGetter {

	Class<?> getType();

	Object getValue(Object instance) throws Exception;
}
