package com.dianping.pigeon.remoting.invoker.route.support;

public interface HashFunction {

	public Integer hash(Object o);

	public Integer hash(String string);
}
