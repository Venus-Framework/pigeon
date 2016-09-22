package com.dianping.pigeon.remoting.common.util;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Created by chenchongze on 16/9/22.
 */
public class GroovyUtils {

    private static final ThreadLocal<GroovyShell> groovyShellThreadLocal = new ThreadLocal<GroovyShell>() {
        @Override
        protected GroovyShell initialValue() {
            return new GroovyShell(Thread.currentThread().getContextClassLoader());
        }
    };

    public static Script getScript(String scriptText) throws Throwable {
        return groovyShellThreadLocal.get().parse(scriptText);
    }
}
