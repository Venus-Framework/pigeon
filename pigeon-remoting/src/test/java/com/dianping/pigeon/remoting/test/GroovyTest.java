package com.dianping.pigeon.remoting.test;

import groovy.lang.*;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.Test;

import java.io.File;

/**
 * Created by chenchongze on 16/9/21.
 */
public class GroovyTest {

    private static final ThreadLocal<GroovyShell> groovyShellThreadLocal = new ThreadLocal<GroovyShell>() {
        @Override
        protected GroovyShell initialValue() {
            return new GroovyShell(Thread.currentThread().getContextClassLoader());
        }
    };

    @Test
    public void test1() {
        String script = "return new com.dianping.pigeon.remoting.test.Person(name:'ccz',age:20);";

        try {
            Script groovyScript = getGroovyScript(script);
            System.out.println("cache script");
            System.out.println(groovyScript.run());
        } catch (Throwable t) {
            System.out.println(t.toString());
        }
    }

    /**
     * 基于groovy shell
     * @param script
     * @return
     * @throws Throwable
     */
    public static Object parseGroovyScript(String script) throws Throwable {
        return new GroovyShell().evaluate(script);
    }

    public static Script getGroovyScript(String script) throws Throwable {
        return groovyShellThreadLocal.get().parse(script);
    }

    @Test
    public void test2() {
        String script = "new com.dianping.pigeon.remoting.test.Person(name:'ccz',age:20);";

        try {
            System.out.println(parse(script));
        } catch (Throwable t) {
            System.out.println(t.toString());
        }
    }

    /**
     * 基于groovy class loader
     * @param script
     * @return
     * @throws Throwable
     */
    public static Object parse(String script) throws Throwable {
        GroovyClassLoader classLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        GroovyCodeSource groovyCodeSource = new GroovyCodeSource(
                "class GroovyRun {\n\tObject run(String s1, String s2) {\n\t\tprintln s1+s2;" + script + "\n\t}\n}", "Groovy","");
        Class testGroovyClass = classLoader.parseClass(groovyCodeSource);
        GroovyObject instance = (GroovyObject) testGroovyClass.newInstance();//proxy

        return instance.invokeMethod("run", new Object[]{"aaa", "bbb"});
    }

    @Test
    public void test11() {
        Binding binding = new Binding();
        CompilerConfiguration config =  new CompilerConfiguration();
        config.setScriptBaseClass("com.dianping.pigeon.remoting.test.MyScript");//MyScript.groovy文件默认在src目录下

        GroovyShell shell = new GroovyShell(Thread.currentThread().getContextClassLoader(), binding, config);
        Person person1 = (Person) shell.evaluate("customScript()");//执行MyScript.groovy脚本的test1方法（非私有）
        System.out.println(person1);//输出返回的对象
    }

    public static void parse() throws Exception {
        GroovyClassLoader classLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        File sourceFile = new File("/Users/chenchongze/Documents/Person.groovy");
        Class testGroovyClass = classLoader.parseClass(new GroovyCodeSource(sourceFile));
        GroovyObject instance = (GroovyObject)testGroovyClass.newInstance();//proxy
        instance.setProperty("name", "ccz");
        instance.invokeMethod("setAge", 20L);
        String str = (String)instance.invokeMethod("toString", null);
        System.out.println(str);
        System.out.println(instance.getMetaClass().getTheClass().getCanonicalName());
        Class.forName(instance.getMetaClass().getTheClass().getCanonicalName());
        //here
        instance = null;
        testGroovyClass = null;
    }
}
