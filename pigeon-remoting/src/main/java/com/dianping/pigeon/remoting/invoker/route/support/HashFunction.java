package com.dianping.pigeon.remoting.invoker.route.balance;

public interface HashFunction {

	public Integer hash(Object o);

	public Integer hash(String string);
}
