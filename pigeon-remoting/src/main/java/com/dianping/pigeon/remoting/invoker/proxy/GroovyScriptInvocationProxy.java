package com.dianping.pigeon.remoting.invoker.proxy;

import groovy.lang.Script;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by chenchongze on 16/9/27.
 */
public class GroovyScriptInvocationProxy implements InvocationHandler {

    private final Script script;

    public GroovyScriptInvocationProxy(Script script) {
        this.script = script;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(script, args);
        }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return script.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return script.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return script.equals(args[0]);
        }

        return script.run();
    }
}
